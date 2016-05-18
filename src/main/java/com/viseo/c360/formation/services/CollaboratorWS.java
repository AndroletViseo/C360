package com.viseo.c360.formation.services;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;

import com.viseo.c360.formation.dao.TrainingDAO;
import com.viseo.c360.formation.domain.collaborator.Collaborator;
import com.viseo.c360.formation.domain.collaborator.RequestTraining;
import com.viseo.c360.formation.domain.training.Training;
import com.viseo.c360.formation.domain.training.TrainingSession;
import com.viseo.c360.formation.dto.collaborator.RequestTrainingDTO;

import com.viseo.c360.formation.dto.training.TrainingSessionDTO;
import com.viseo.c360.formation.exceptions.PersistentObjectNotFoundException;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionService;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.viseo.c360.formation.dao.CollaboratorDAO;


@RestController
public class CollaboratorWS {

	@Inject
	CollaboratorDAO collaboratorDAO;
	@Inject
	TrainingDAO trainingDAO;
	@Inject
	ConversionService conversionService;
		
	@RequestMapping(value = "${endpoint.collaborators}",method = RequestMethod.POST)
    @ResponseBody
    public boolean addCollaborator(@Valid @RequestBody Collaborator myCollaborator, BindingResult bindingResult){
		if(!(bindingResult.hasErrors()) && !collaboratorDAO.isPersonnalIdNumberPersisted(myCollaborator.getPersonnalIdNumber())){
			collaboratorDAO.addCollaborator(myCollaborator);
			return true;
		}
		return false;
    }
	
	@RequestMapping(value = "${endpoint.collaborators}", method = RequestMethod.GET)
	@ResponseBody
    public List<Collaborator> getAllCollaborators(){
		return collaboratorDAO.getAllCollaborators();
	}
	
	@RequestMapping(value = "${endpoint.requests}",method = RequestMethod.POST)
    @ResponseBody
    public boolean addRequestTraining(@Valid @RequestBody RequestTrainingDTO requestTrainingDto, BindingResult bindingResult){
		if(bindingResult.hasErrors()) return false;
		RequestTraining myRequestTraining = new RequestTraining();
		Collaborator collaborator = collaboratorDAO.getCollaborator(requestTrainingDto.getCollaborator());
		Training training = trainingDAO.getTraining(requestTrainingDto.getTraining().getId());
		if(collaborator == null || training == null) return false;
		myRequestTraining.setCollaborator(collaborator);
		myRequestTraining.setTraining(training);
		for(TrainingSessionDTO trainingSessionDto : requestTrainingDto.getTrainingSessions()){
			try {
				myRequestTraining.addListSession(
						conversionService.convert(trainingSessionDto,TrainingSession.class)
				);
			} catch (ConversionException e) {
				e.printStackTrace();
				return false;
			}
		}
		collaboratorDAO.addRequestTraining(myRequestTraining);
		return true;
    }

	@RequestMapping(value = "${endpoint.collaboratorsbysession}",method = RequestMethod.PUT)
	@ResponseBody
	public boolean affectCollaboratorsTrainingSession(@PathVariable Long id, @Valid @RequestBody List<Collaborator> collaborators, BindingResult bindingResult){
		try {
			 TrainingSession trainingSession = trainingDAO.getSessionTraining(id);
			 if(trainingSession == null) throw new PersistentObjectNotFoundException(id, TrainingSession.class);
			 collaboratorDAO.affectCollaboratorsTrainingSession(trainingSession, collaborators);
			 return true;
		} catch (PersistentObjectNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	@RequestMapping(value = "${endpoint.collaboratorsNotAffectedBySession}", method = RequestMethod.GET)
	@ResponseBody
	public List<Collaborator> getNotAffectedCollaboratorsList(@PathVariable Long id){
		try {
			TrainingSession trainingSession = trainingDAO.getSessionTraining(id);
			if(trainingSession == null) throw new PersistentObjectNotFoundException(id, TrainingSession.class);
			return collaboratorDAO.getNotAffectedCollaborators(trainingSession);
		} catch (PersistentObjectNotFoundException e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

	@RequestMapping(value = "${endpoint.collaboratorsAffectedBySession}", method = RequestMethod.GET)
	@ResponseBody
	public List<Collaborator> getAffectedCollaboratorsList(@PathVariable Long id){
		try {
			TrainingSession trainingSession = trainingDAO.getSessionTraining(id);
			if(trainingSession == null) throw new PersistentObjectNotFoundException(id, TrainingSession.class);
			return trainingSession.getCollaborators();
		} catch (PersistentObjectNotFoundException e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
}