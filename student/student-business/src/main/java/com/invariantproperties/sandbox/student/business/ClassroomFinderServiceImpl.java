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
package com.invariantproperties.sandbox.student.business;

import static com.invariantproperties.sandbox.student.specification.ClassroomSpecifications.testRunIs;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.invariantproperties.sandbox.student.domain.Classroom;
import com.invariantproperties.sandbox.student.domain.TestRun;
import com.invariantproperties.sandbox.student.repository.ClassroomRepository;

/**
 * Implementation of ClassroomService
 * 
 * @author Bear Giles <bgiles@coyotesong.com>
 */
@Service
public class ClassroomFinderServiceImpl implements ClassroomFinderService {
    private static final Logger log = LoggerFactory.getLogger(ClassroomFinderServiceImpl.class);

    @Resource
    private ClassroomRepository classroomRepository;

    /**
     * Default constructor
     */
    public ClassroomFinderServiceImpl() {

    }

    /**
     * Constructor used in unit tests
     */
    ClassroomFinderServiceImpl(ClassroomRepository classroomRepository) {
        this.classroomRepository = classroomRepository;
    }

    /**
     * @see com.invariantproperties.sandbox.student.business.FinderService#
     *      count()
     */
    @Transactional(readOnly = true)
    @Override
    public long count() {
        return countByTestRun(null);
    }

    /**
     * @see com.invariantproperties.sandbox.student.business.FinderService#
     *      countByTestRun(com.invariantproperties.sandbox.student.domain.TestRun)
     */
    @Transactional(readOnly = true)
    @Override
    public long countByTestRun(TestRun testRun) {
        long count = 0;
        try {
            count = classroomRepository.count(testRunIs(testRun));
        } catch (DataAccessException e) {
            if (!(e instanceof UnitTestException)) {
                log.info("internal error retrieving classroom count by " + testRun, e);
            }
            throw new PersistenceException("unable to count classrooms by " + testRun, e, 0);
        }

        return count;
    }

    /**
     * @see com.invariantproperties.sandbox.student.business.ClassroomFinderService#
     *      findAllClassrooms()
     */
    @Transactional(readOnly = true)
    @Override
    public List<Classroom> findAllClassrooms() {
        return findClassroomsByTestRun(null);
    }

    /**
     * @see com.invariantproperties.sandbox.student.business.ClassroomFinderService#
     *      findClassroomById(java.lang.Integer)
     */
    @Transactional(readOnly = true)
    @Override
    public Classroom findClassroomById(Integer id) {
        Classroom classroom = null;
        try {
            classroom = classroomRepository.findOne(id);
        } catch (DataAccessException e) {
            if (!(e instanceof UnitTestException)) {
                log.info("internal error retrieving classroom: " + id, e);
            }
            throw new PersistenceException("unable to find classroom by id", e, id);
        }

        if (classroom == null) {
            throw new ObjectNotFoundException(id);
        }

        return classroom;
    }

    /**
     * @see com.invariantproperties.sandbox.student.business.ClassroomFinderService#
     *      findClassroomByUuid(java.lang.String)
     */
    @Transactional(readOnly = true)
    @Override
    public Classroom findClassroomByUuid(String uuid) {
        Classroom classroom = null;
        try {
            classroom = classroomRepository.findClassroomByUuid(uuid);
        } catch (DataAccessException e) {
            if (!(e instanceof UnitTestException)) {
                log.info("internal error retrieving classroom: " + uuid, e);
            }
            throw new PersistenceException("unable to find classroom by uuid", e, uuid);
        }

        if (classroom == null) {
            throw new ObjectNotFoundException(uuid);
        }

        return classroom;
    }

    /**
     * @see com.invariantproperties.sandbox.student.business.ClassroomFinderService#
     *      findClassroomsByTestRun(com.invariantproperties.sandbox.student.common.TestRun)
     */
    @Transactional(readOnly = true)
    @Override
    public List<Classroom> findClassroomsByTestRun(TestRun testRun) {
        List<Classroom> classrooms = null;

        try {
            classrooms = classroomRepository.findAll(testRunIs(testRun));
        } catch (DataAccessException e) {
            if (!(e instanceof UnitTestException)) {
                log.info("error loading list of classrooms: " + e.getMessage(), e);
            }
            throw new PersistenceException("unable to get list of classrooms.", e);
        }

        return classrooms;
    }
}
