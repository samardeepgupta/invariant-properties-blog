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
package com.invariantproperties.sandbox.student.webservice.server.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.invariantproperties.sandbox.student.business.InstructorFinderService;
import com.invariantproperties.sandbox.student.business.InstructorManagerService;
import com.invariantproperties.sandbox.student.business.ObjectNotFoundException;
import com.invariantproperties.sandbox.student.business.TestRunService;
import com.invariantproperties.sandbox.student.domain.Instructor;
import com.invariantproperties.sandbox.student.domain.TestRun;

@Service
@Path("/instructor")
public class InstructorResource extends AbstractResource {
    private static final Logger log = Logger.getLogger(InstructorResource.class);
    private static final Instructor[] EMPTY_INSTRUCTOR_ARRAY = new Instructor[0];

    @Context
    UriInfo uriInfo;

    @Context
    Request request;

    @Resource
    private InstructorFinderService finder;

    @Resource
    private InstructorManagerService manager;

    @Resource
    private TestRunService testService;

    /**
     * Default constructor.
     */
    public InstructorResource() {

    }

    /**
     * Unit test constructor.
     * 
     * @param manager
     */
    InstructorResource(InstructorFinderService finder, TestRunService testService) {
        this(finder, null, testService);
    }

    /**
     * Unit test constructor.
     * 
     * @param manager
     */
    InstructorResource(InstructorManagerService manager, TestRunService testService) {
        this(null, manager, testService);
    }

    /**
     * Unit test constructor.
     * 
     * @param manager
     */
    InstructorResource(InstructorFinderService finder, InstructorManagerService manager, TestRunService testService) {
        this.finder = finder;
        this.manager = manager;
        this.testService = testService;
    }

    /**
     * Get all Instructors.
     * 
     * @return
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_XML })
    public Response findAllInstructors() {
        log.debug("InstructorResource: findAllInstructors()");

        Response response = null;
        try {
            List<Instructor> instructors = finder.findAllInstructors();

            List<Instructor> results = new ArrayList<Instructor>(instructors.size());
            for (Instructor instructor : instructors) {
                results.add(scrubInstructor(instructor));
            }

            response = Response.ok(results.toArray(EMPTY_INSTRUCTOR_ARRAY)).build();
        } catch (Exception e) {
            if (!(e instanceof UnitTestException)) {
                log.info("unhandled exception", e);
            }
            response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }

        return response;
    }

    /**
     * Create a Instructor.
     * 
     * @param req
     * @return
     */
    @POST
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.TEXT_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_XML })
    public Response createInstructor(NameAndEmailAddress req) {
        log.debug("InstructorResource: createInstructor()");

        final String name = req.getName();
        if ((name == null) || name.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity("'name' is required'").build();
        }

        final String email = req.getEmailAddress();
        if ((email == null) || email.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity("'email' is required'").build();
        }

        Response response = null;

        try {
            Instructor instructor = null;

            if (req.getTestUuid() != null) {
                TestRun testRun = testService.findTestRunByUuid(req.getTestUuid());
                if (testRun != null) {
                    instructor = manager.createInstructorForTesting(name, email, testRun);
                } else {
                    response = Response.status(Status.BAD_REQUEST).entity("unknown test UUID").build();
                }
            } else {
                instructor = manager.createInstructor(name, email);
            }

            if (instructor == null) {
                response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
            } else {
                response = Response.created(URI.create(instructor.getUuid())).entity(scrubInstructor(instructor))
                        .build();
            }
        } catch (Exception e) {
            if (!(e instanceof UnitTestException)) {
                log.info("unhandled exception", e);
            }
            response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }

        return response;
    }

    /**
     * Get a specific Instructor.
     * 
     * @param uuid
     * @return
     */
    @Path("/{instructorId}")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_XML })
    public Response getInstructor(@PathParam("instructorId") String id) {
        log.debug("InstructorResource: getInstructor()");

        Response response = null;
        try {
            Instructor instructor = finder.findInstructorByUuid(id);
            response = Response.ok(scrubInstructor(instructor)).build();
        } catch (ObjectNotFoundException e) {
            response = Response.status(Status.NOT_FOUND).build();
        } catch (Exception e) {
            if (!(e instanceof UnitTestException)) {
                log.info("unhandled exception", e);
            }
            response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }

        return response;
    }

    /**
     * Update a Instructor.
     * 
     * FIXME: what about uniqueness violations?
     * 
     * @param id
     * @param req
     * @return
     */
    @Path("/{instructorId}")
    @POST
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.TEXT_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_XML })
    public Response updateInstructor(@PathParam("instructorId") String id, NameAndEmailAddress req) {
        log.debug("InstructorResource: updateInstructor()");

        final String name = req.getName();
        if ((name == null) || name.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity("'name' is required'").build();
        }

        final String email = req.getEmailAddress();
        if ((email == null) || email.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity("'email' is required'").build();
        }

        Response response = null;
        try {
            final Instructor instructor = finder.findInstructorByUuid(id);
            final Instructor updatedInstructor = manager.updateInstructor(instructor, name, email);
            response = Response.ok(scrubInstructor(updatedInstructor)).build();
        } catch (ObjectNotFoundException exception) {
            response = Response.status(Status.NOT_FOUND).build();
        } catch (Exception e) {
            if (!(e instanceof UnitTestException)) {
                log.info("unhandled exception", e);
            }
            response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }

        return response;
    }

    /**
     * Delete a Instructor.
     * 
     * @param id
     * @return
     */
    @Path("/{instructorId}")
    @DELETE
    public Response deleteInstructor(@PathParam("instructorId") String id, @PathParam("version") int version) {
        log.debug("InstructorResource: deleteInstructor()");

        Response response = null;
        try {
            manager.deleteInstructor(id, version);
            response = Response.noContent().build();
        } catch (ObjectNotFoundException exception) {
            response = Response.noContent().build();
        } catch (Exception e) {
            if (!(e instanceof UnitTestException)) {
                log.info("unhandled exception", e);
            }
            response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }

        return response;
    }
}