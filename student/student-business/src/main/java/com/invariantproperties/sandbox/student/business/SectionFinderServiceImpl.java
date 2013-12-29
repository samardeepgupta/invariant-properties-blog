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

import static com.invariantproperties.sandbox.student.specification.SectionSpecifications.testRunIs;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.invariantproperties.sandbox.student.domain.Section;
import com.invariantproperties.sandbox.student.domain.TestRun;
import com.invariantproperties.sandbox.student.repository.SectionRepository;

/**
 * Implementation of SectionService
 * 
 * @author Bear Giles <bgiles@coyotesong.com>
 */
@Service
public class SectionFinderServiceImpl implements SectionFinderService {
    private static final Logger log = LoggerFactory.getLogger(SectionFinderServiceImpl.class);

    @Resource
    private SectionRepository sectionRepository;

    /**
     * Default constructor
     */
    public SectionFinderServiceImpl() {

    }

    /**
     * Constructor used in unit tests
     */
    SectionFinderServiceImpl(SectionRepository sectionRepository) {
        this.sectionRepository = sectionRepository;
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
            count = sectionRepository.count(testRunIs(testRun));
        } catch (DataAccessException e) {
            if (!(e instanceof UnitTestException)) {
                log.info("internal error retrieving classroom count by " + testRun, e);
            }
            throw new PersistenceException("unable to count classrooms by " + testRun, e, 0);
        }

        return count;
    }

    /**
     * @see com.invariantproperties.sandbox.student.business.SectionFinderService#
     *      findAllSections()
     */
    @Transactional(readOnly = true)
    @Override
    public List<Section> findAllSections() {
        return findSectionsByTestRun(null);
    }

    /**
     * @see com.invariantproperties.sandbox.student.business.SectionFinderService#
     *      findSectionById(java.lang.Integer)
     */
    @Transactional(readOnly = true)
    @Override
    public Section findSectionById(Integer id) {
        Section section = null;
        try {
            section = sectionRepository.findOne(id);
        } catch (DataAccessException e) {
            if (!(e instanceof UnitTestException)) {
                log.info("internal error retrieving section: " + id, e);
            }
            throw new PersistenceException("unable to find section by id", e, id);
        }

        if (section == null) {
            throw new ObjectNotFoundException(id);
        }

        return section;
    }

    /**
     * @see com.invariantproperties.sandbox.student.business.SectionFinderService#
     *      findSectionByUuid(java.lang.String)
     */
    @Transactional(readOnly = true)
    @Override
    public Section findSectionByUuid(String uuid) {
        Section section = null;
        try {
            section = sectionRepository.findSectionByUuid(uuid);
        } catch (DataAccessException e) {
            if (!(e instanceof UnitTestException)) {
                log.info("internal error retrieving section: " + uuid, e);
            }
            throw new PersistenceException("unable to find section by uuid", e, uuid);
        }

        if (section == null) {
            throw new ObjectNotFoundException(uuid);
        }

        return section;
    }

    /**
     * @see com.invariantproperties.sandbox.student.business.SectionFinderService#
     *      findSectionsByTestRun(com.invariantproperties.sandbox.student.common.TestRun)
     */
    @Transactional(readOnly = true)
    @Override
    public List<Section> findSectionsByTestRun(TestRun testRun) {
        List<Section> sections = null;

        try {
            sections = sectionRepository.findAll(testRunIs(testRun));
        } catch (DataAccessException e) {
            if (!(e instanceof UnitTestException)) {
                log.info("error loading list of sections: " + e.getMessage(), e);
            }
            throw new PersistenceException("unable to get list of sections.", e);
        }

        return sections;
    }
}
