package com.viseo.c360.formation.services;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import com.viseo.c360.formation.converters.collaborator.CollaboratorToIdentity;
import com.viseo.c360.formation.converters.collaborator.DescriptionToCollaborator;
import com.viseo.c360.formation.dao.CollaboratorDAO;
import com.viseo.c360.formation.domain.collaborator.RequestTraining;
import com.viseo.c360.formation.domain.training.Topic;
import com.viseo.c360.formation.domain.training.TrainingSession;
import com.viseo.c360.formation.dto.collaborator.CollaboratorDescription;
import com.viseo.c360.formation.dto.collaborator.CollaboratorIdentity;
import com.viseo.c360.formation.dto.collaborator.RequestTrainingDescription;
import com.viseo.c360.formation.converters.collaborator.CollaboratorToDescription;
import com.viseo.c360.formation.converters.requestTraining.DescriptionToRequestTraining;
import com.viseo.c360.formation.converters.requestTraining.RequestTrainingToDescription;
import com.viseo.c360.formation.converters.trainingsession.TrainingSessionToDescription;
import com.viseo.c360.formation.dto.training.TrainingSessionDescription;
import com.viseo.c360.formation.exceptions.dao.UniqueFieldException;
import com.viseo.c360.formation.exceptions.dao.util.UniqueFieldErrors;
import com.viseo.c360.formation.exceptions.dao.util.ExceptionUtil;
import com.viseo.c360.formation.dao.TrainingDAO;
import com.viseo.c360.formation.domain.collaborator.Collaborator;


import com.viseo.c360.formation.exceptions.C360Exception;
import com.viseo.c360.formation.exceptions.dao.PersistentObjectNotFoundException;

import org.springframework.core.convert.ConversionException;
import org.springframework.web.bind.annotation.*;


@RestController
public class CollaboratorWS {

    @Inject
    CollaboratorDAO collaboratorDAO;
    @Inject
    TrainingDAO trainingDAO;

    @Inject
    ExceptionUtil exceptionUtil;


    @RequestMapping(value = "${endpoint.signup}", method = RequestMethod.POST)
    @ResponseBody
    public CollaboratorDescription addCollaborator(@RequestBody CollaboratorDescription collaboratorDescription) {
        try {
            Collaborator collaborator = collaboratorDAO.addCollaborator(new DescriptionToCollaborator().convert(collaboratorDescription));
            return new CollaboratorToDescription().convert(collaborator);
        } catch (PersistenceException pe) {
            UniqueFieldErrors uniqueFieldErrors = exceptionUtil.getUniqueFieldError(pe);
            if(uniqueFieldErrors == null) throw new C360Exception(pe);
            else throw new UniqueFieldException(uniqueFieldErrors.getField());
        }
    }

    @RequestMapping(value = "${endpoint.collaborators}", method = RequestMethod.GET)
    @ResponseBody
    public List<CollaboratorIdentity> getAllCollaborators() {
        try {
            return new CollaboratorToIdentity().convert(collaboratorDAO.getAllCollaborators());
        } catch (ConversionException e) {
            e.printStackTrace();
            throw new C360Exception(e);
        }
    }

    @RequestMapping(value = "${endpoint.collaboratorsNotAffectedBySession}", method = RequestMethod.GET)
    @ResponseBody
    public List<CollaboratorIdentity> getNotAffectedCollaboratorsList(@PathVariable Long id) {
        try {
            TrainingSession trainingSession = trainingDAO.getSessionTraining(id);
            if (trainingSession == null) throw new PersistentObjectNotFoundException(id, TrainingSession.class);
            return new CollaboratorToIdentity().convert(collaboratorDAO.getNotAffectedCollaborators(trainingSession));
        } catch (PersistentObjectNotFoundException e) {
            e.printStackTrace();
            throw new C360Exception(e);
        }
    }

    @RequestMapping(value = "${endpoint.collaboratorsAffectedBySession}", method = RequestMethod.GET)
    @ResponseBody
    public List<CollaboratorIdentity> getAffectedCollaboratorsList(@PathVariable Long id) {
        try {
            TrainingSession trainingSession = trainingDAO.getSessionTraining(id);
            if (trainingSession == null) throw new PersistentObjectNotFoundException(id, TrainingSession.class);
            return new CollaboratorToIdentity().convert(trainingSession.getCollaborators());
        } catch (PersistentObjectNotFoundException e) {
            e.printStackTrace();
            throw new C360Exception(e);
        }
    }


    @RequestMapping(value = "${endpoint.requests}", method = RequestMethod.POST)
    @ResponseBody
    public RequestTrainingDescription addRequestTraining(@RequestBody RequestTrainingDescription requestTrainingDescription) {
        Topic topic = trainingDAO.getTopic(requestTrainingDescription.getTrainingDescription().getTopicDescription().getId());
        Collaborator collaborator = collaboratorDAO.getCollaborator(requestTrainingDescription.getCollaboratorIdentity().getId());
        RequestTraining requestTraining = new DescriptionToRequestTraining().convert(requestTrainingDescription, collaborator, topic);
        requestTraining = collaboratorDAO.addRequestTraining(requestTraining);
        return new RequestTrainingToDescription().convert(requestTraining);
    }

    @RequestMapping(value = "${endpoint.collaboratorsbysession}", method = RequestMethod.PUT)
    @ResponseBody
    public TrainingSessionDescription updateCollaboratorsTrainingSession(@PathVariable Long idTrainingSession, @RequestBody List<CollaboratorIdentity> collaboratorIdentities) {
        try {
            TrainingSession trainingSession = trainingDAO.getSessionTraining(idTrainingSession);
            if (trainingSession == null) throw new PersistentObjectNotFoundException(idTrainingSession, TrainingSession.class);
            trainingSession = collaboratorDAO.affectCollaboratorsTrainingSession(trainingSession, collaboratorIdentities);
            return new TrainingSessionToDescription().convert(trainingSession);
        } catch (PersistentObjectNotFoundException e) {
            e.printStackTrace();
            throw new C360Exception(e);
        }
    }

    @RequestMapping(value = "${endpoint.collaboratorsRequestingListByTrainingSession}", method = RequestMethod.GET)
    @ResponseBody
    public List<CollaboratorIdentity> getCollaboratorsRequestingListByTrainingSession(@PathVariable Long id) {
        TrainingSession trainingSession = null;
        try {
            trainingSession = trainingDAO.getSessionTraining(id);
            return new CollaboratorToIdentity().convert(collaboratorDAO.getCollaboratorsRequestingBySession(trainingSession));
        } catch (PersistentObjectNotFoundException e) {
            throw new C360Exception(e);
        }
    }
}