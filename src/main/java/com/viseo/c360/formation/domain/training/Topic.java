package com.viseo.c360.formation.domain.training;


import com.sun.istack.internal.NotNull;
import com.viseo.c360.formation.domain.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class Topic extends BaseEntity {

    @NotNull
    String name;

    public Topic() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}