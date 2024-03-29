/*
 * This code was written by Bear Giles <bgiles@coyotesong.com> and he
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Any contributions made by others are licensed to this project under
 * one or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 * Copyright (c) 2013 Bear Giles <bgiles@coyotesong.com>
 */
package com.invariantproperties.sandbox.student.maintenance.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.invariantproperties.sandbox.student.business.ClassroomFinderService;
import com.invariantproperties.sandbox.student.business.ObjectNotFoundException;
import com.invariantproperties.sandbox.student.domain.Classroom;
import com.invariantproperties.sandbox.student.domain.TestRun;
import com.invariantproperties.sandbox.student.webservice.client.ClassroomFinderRestClient;
import com.invariantproperties.sandbox.student.webservice.client.ClassroomManagerRestClient;
import com.invariantproperties.sandbox.student.webservice.client.impl.ClassroomFinderRestClientImpl;
import com.invariantproperties.sandbox.student.webservice.client.impl.ClassroomManagerRestClientImpl;

public class ClassroomFinderServiceTapestryImpl implements ClassroomFinderService {
    private final ClassroomFinderRestClient finder;

    // private final Map<String, Classroom> cache = new HashMap<>();

    public ClassroomFinderServiceTapestryImpl() {
        // resource should be loaded as tapestry resource
        final String resource = "http://localhost:8080/student-ws-webapp/rest/classroom/";
        finder = new ClassroomFinderRestClientImpl(resource);
        initCache(new ClassroomManagerRestClientImpl(resource));
    }

    @Override
    public long count() {
        // FIXME: grossly inefficient but good enough for now.
        return finder.getAllClassrooms().length;
    }

    // @Override
    public long countByTestRun(TestRun testRun) {
        // FIXME: grossly inefficient but good enough for now.
        return finder.getAllClassrooms().length;
    }

    @Override
    public Classroom findClassroomById(Integer id) {
        // unsupported operation!
        throw new ObjectNotFoundException(id);
    }

    @Override
    public Classroom findClassroomByUuid(String uuid) {
        return finder.getClassroom(uuid);
    }

    @Override
    public List<Classroom> findAllClassrooms() {
        // return new ArrayList<Classroom>(cache.values());
        return Arrays.asList(finder.getAllClassrooms());
    }

    public List<Classroom> findClassrooms(int maxResults) {
        return Arrays.asList(finder.getAllClassrooms());
    }

    public List<Classroom> findClassrooms(String partialName, int maxResults) {
        return Collections.emptyList();
    }

    public List<Classroom> findClassroomsByName(String name) {
        return Collections.emptyList();
    }

    @Override
    public List<Classroom> findClassroomsByTestRun(TestRun testRun) {
        // return new ArrayList<Classroom>(cache.values());
        return Arrays.asList(finder.getAllClassrooms());
    }

    public long countClassrooms(String name) {
        return 0;
    }

    public List<Classroom> findClassrooms(String name, int startIndex, int maxResults) {
        return Collections.emptyList();
    }

    // private Object findClassrooms(boolean counting, String name, int
    // startIndex,
    // int maxResults) {
    // return null;
    // }

    // public List<Classroom> findClassrooms(int startIndex, int maxResults,
    // List<SortCriterion> sortCriteria) {
    // return Collections.emptyList();
    // }

    private void initCache(ClassroomManagerRestClient manager) {
        manager.createClassroom("eng 1-01");
        manager.createClassroom("eng 2-01");
        manager.createClassroom("eng 2-02");
    }
}