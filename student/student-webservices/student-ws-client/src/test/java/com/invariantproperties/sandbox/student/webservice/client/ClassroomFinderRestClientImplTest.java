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
package com.invariantproperties.sandbox.student.webservice.client;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;

import com.invariantproperties.sandbox.student.domain.Classroom;
import com.invariantproperties.sandbox.student.webservice.client.impl.ClassroomFinderRestClientImpl;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Unit tests for ClassroomFinderRestClientImpl. Remember that we want to test
 * the behavior, not the implementation.
 * 
 * @author Bear Giles <bgiles@coyotesong.com>
 */
public class ClassroomFinderRestClientImplTest {
    private static final String UUID = "uuid";

    @Test
    public void testGetAllClassroomsEmpty() {
        ClassroomFinderRestClient client = new FinderClassroomMock(200, new Classroom[0]);
        Classroom[] results = client.getAllClassrooms();
        assertEquals(0, results.length);
    }

    @Test
    public void testGetAllClassroomsNonEmpty() {
        Classroom classroom = new Classroom();
        classroom.setUuid(UUID);
        ClassroomFinderRestClient client = new FinderClassroomMock(200, new Classroom[] { classroom });
        Classroom[] results = client.getAllClassrooms();
        assertEquals(1, results.length);
    }

    @Test(expected = RestClientFailureException.class)
    public void testGetAllClassroomsError() {
        ClassroomFinderRestClient client = new FinderClassroomMock(500, null);
        client.getAllClassrooms();
    }

    @Test
    public void testGetClassroom() {
        Classroom expected = new Classroom();
        expected.setUuid(UUID);
        ClassroomFinderRestClient client = new FinderClassroomMock(200, expected);
        Classroom actual = client.getClassroom(expected.getUuid());
        assertEquals(expected.getUuid(), actual.getUuid());
        // assertEquals(ClassroomRestClientMock.RESOURCE + classroom.getUuid(),
        // actual.getSelf());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testGetClassroomMissing() {
        ClassroomFinderRestClient client = new FinderClassroomMock(404, null);
        client.getClassroom(UUID);
    }

    @Test(expected = RestClientFailureException.class)
    public void testGetClassroomError() {
        ClassroomFinderRestClient client = new FinderClassroomMock(500, null);
        client.getClassroom(UUID);
    }
}

/**
 * ClassroomFinderRestClientImpl extended to mock jersey API. This class
 * requires implementation details.
 */
class FinderClassroomMock extends ClassroomFinderRestClientImpl {
    static final String RESOURCE = "test://rest/classroom/";
    private Client client;
    private WebResource webResource;
    private WebResource.Builder webResourceBuilder;
    private ClientResponse response;
    private final int status;
    private final Object results;

    FinderClassroomMock(int status, Object results) {
        super(RESOURCE);
        this.status = status;
        this.results = results;
    }

    /**
     * Override createClient() so it returns mocked object. These expectations
     * will handle basic CRUD operations, more advanced functionality will
     * require inspecting JSON payload of POST call.
     */
    @SuppressWarnings("unchecked")
    @Override
    Client createClient() {
        client = Mockito.mock(Client.class);
        webResource = Mockito.mock(WebResource.class);
        webResourceBuilder = Mockito.mock(WebResource.Builder.class);
        response = Mockito.mock(ClientResponse.class);
        when(client.resource(any(String.class))).thenReturn(webResource);
        when(webResource.accept(any(String.class))).thenReturn(webResourceBuilder);
        when(webResource.type(any(String.class))).thenReturn(webResourceBuilder);
        when(webResourceBuilder.accept(any(String.class))).thenReturn(webResourceBuilder);
        when(webResourceBuilder.type(any(String.class))).thenReturn(webResourceBuilder);
        when(webResourceBuilder.get(eq(ClientResponse.class))).thenReturn(response);
        when(webResourceBuilder.post(eq(ClientResponse.class), any(String.class))).thenReturn(response);
        when(webResourceBuilder.put(eq(ClientResponse.class), any(String.class))).thenReturn(response);
        when(webResourceBuilder.delete(eq(ClientResponse.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(status);
        when(response.getEntity(any(Class.class))).thenReturn(results);
        return client;
    }
}
