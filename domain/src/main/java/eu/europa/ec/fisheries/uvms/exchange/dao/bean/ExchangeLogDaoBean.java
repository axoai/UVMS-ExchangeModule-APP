/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.exchange.dao.bean;

import eu.europa.ec.fisheries.schema.exchange.v1.ExchangeHistoryListQuery;
import eu.europa.ec.fisheries.schema.exchange.v1.TypeRefType;
import eu.europa.ec.fisheries.uvms.exchange.constant.ExchangeConstants;
import eu.europa.ec.fisheries.uvms.exchange.dao.Dao;
import eu.europa.ec.fisheries.uvms.exchange.dao.ExchangeLogDao;
import eu.europa.ec.fisheries.uvms.exchange.entity.exchangelog.ExchangeLog;
import eu.europa.ec.fisheries.uvms.exchange.entity.exchangelog.ExchangeLogStatus;
import eu.europa.ec.fisheries.uvms.exchange.exception.ExchangeDaoException;
import eu.europa.ec.fisheries.uvms.exchange.exception.NoEntityFoundException;
import eu.europa.ec.fisheries.uvms.exchange.model.util.DateUtils;
import eu.europa.ec.fisheries.uvms.exchange.search.ExchangeSearchField;
import eu.europa.ec.fisheries.uvms.exchange.search.SearchFieldMapper;
import eu.europa.ec.fisheries.uvms.exchange.search.SearchValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 **/
@Stateless
public class ExchangeLogDaoBean extends Dao implements ExchangeLogDao {

    private final static Logger LOG = LoggerFactory.getLogger(ExchangeLogDaoBean.class);

    @Override
	public List<ExchangeLogStatus> getExchangeLogStatusHistory(String sql, ExchangeHistoryListQuery searchQuery) throws ExchangeDaoException {
    	try {
    		LOG.debug("SQL query for status history " + sql);
    		TypedQuery<ExchangeLogStatus> query = em.createQuery(sql, ExchangeLogStatus.class);
    		if(searchQuery.getStatus() != null && !searchQuery.getStatus().isEmpty()) {
    			query.setParameter("status", searchQuery.getStatus());
    		}
    		if(searchQuery.getType() != null && !searchQuery.getType().isEmpty()) {
    			query.setParameter("type", searchQuery.getType());
    		}
    		if(searchQuery.getTypeRefDateFrom() != null) {
    			Date from = searchQuery.getTypeRefDateFrom();
    			query.setParameter("from", from);
    		}
    		if(searchQuery.getTypeRefDateTo() != null) {
    			Date to = searchQuery.getTypeRefDateTo();
    			query.setParameter("to", to);
    		}
    		return query.getResultList();
        } catch (IllegalArgumentException e) {
            LOG.error("[ Error getting exchangelog status list ] " + e.getMessage());
            throw new ExchangeDaoException("[ Error when getting search list ] ");
        } catch (Exception e) {
            LOG.error("[ Error getting exchangelog status list " + e.getMessage());
            throw new ExchangeDaoException("[ Error when getting search list ] ");
        }	
	}
    
    @Override
    public List<ExchangeLog> getExchangeLogListPaginated(Integer page, Integer listSize, String sql, List<SearchValue> searchKeyValues) throws ExchangeDaoException {
        try {
            LOG.debug("SQL QUERY IN LIST PAGINATED: " + sql);
            TypedQuery<ExchangeLog> query = em.createQuery(sql, ExchangeLog.class);

            HashMap<ExchangeSearchField, List<SearchValue>> orderedValues = SearchFieldMapper.combineSearchFields(searchKeyValues);

            for (Map.Entry<ExchangeSearchField, List<SearchValue>> criteria : orderedValues.entrySet()) {
            	if(criteria.getValue().size() > 1) {
            		query.setParameter(criteria.getKey().getSQLReplacementToken(), criteria.getValue());
            	} else {
            		switch (criteria.getKey()) {
                    case FROM_DATE:
                    	String fromDate = criteria.getValue().get(0).getValue(); //Only one
                        query.setParameter(criteria.getKey().getSQLReplacementToken(), DateUtils.parseToUTCDateTime(fromDate));
                        break;
                    case TO_DATE:
                    	String toDate = criteria.getValue().get(0).getValue(); //Only one
                        query.setParameter(criteria.getKey().getSQLReplacementToken(), DateUtils.parseToUTCDateTime(toDate));
                        break;
            		}
            	}
            }

            query.setFirstResult(listSize * (page - 1));
            query.setMaxResults(listSize);

            return query.getResultList();
        } catch (IllegalArgumentException e) {
            LOG.error("[ Error getting exchangelog list paginated ] {}", e.getMessage());
            throw new ExchangeDaoException("[ Error when getting list ] ");
        } catch (Exception e) {
            LOG.error("[ Error getting exchangelog list paginated ]  {}", e.getMessage());
            throw new ExchangeDaoException("[ Error when getting list ] ");
        }
    }

    @Override
    public Long getExchangeLogListSearchCount(String countSql, List<SearchValue> searchKeyValues) throws ExchangeDaoException {
        LOG.debug("SQL QUERY IN LIST COUNT: " + countSql);
        TypedQuery<Long> query = em.createQuery(countSql, Long.class);

        HashMap<ExchangeSearchField, List<SearchValue>> orderedValues = SearchFieldMapper.combineSearchFields(searchKeyValues);

        for (Map.Entry<ExchangeSearchField, List<SearchValue>> criteria : orderedValues.entrySet()) {
        	if(criteria.getValue().size() > 1) {
        		query.setParameter(criteria.getKey().getSQLReplacementToken(), criteria.getValue());
        	} else {
        		switch (criteria.getKey()) {
                case FROM_DATE:
                	String fromDate = criteria.getValue().get(0).getValue(); //Only one
                    query.setParameter(criteria.getKey().getSQLReplacementToken(), DateUtils.parseToUTCDateTime(fromDate));
                    break;
                case TO_DATE:
                	String toDate = criteria.getValue().get(0).getValue(); //Only one
                    query.setParameter(criteria.getKey().getSQLReplacementToken(), DateUtils.parseToUTCDateTime(toDate));
                    break;
        		}
        	}
        }

        return query.getSingleResult();
    }
    
    @Override
    public ExchangeLog createLog(ExchangeLog log) throws ExchangeDaoException {
        try {
            em.persist(log);
            return log;
        } catch (PersistenceException e) {
            LOG.error("[ Error creating log ]", e);
            throw new ExchangeDaoException("[ Error creating log ] ", e);
        }
    }

	@Override
	public ExchangeLog getExchangeLogByGuid(String logGuid) throws NoEntityFoundException, ExchangeDaoException {
        try {
            TypedQuery<ExchangeLog> query = em.createNamedQuery(ExchangeConstants.LOG_BY_GUID, ExchangeLog.class);
            query.setParameter("guid", logGuid);
            return query.getSingleResult();
        } catch (NoResultException e) {
            LOG.error("[ Error when getting entity by ID. ] {}", e.getMessage());
            throw new NoEntityFoundException("[ Error when getting entity by ID. ]");
        } catch (Exception e) {
            LOG.error("[ Error when getting entity by ID. ] {}", e.getMessage());
            throw new ExchangeDaoException("[ Error when getting entity by ID. ] ");
        }
	}

	@Override
	public ExchangeLog getExchangeLogByTypeRefAndGuid(String typeRefGuid, TypeRefType type) throws NoEntityFoundException, ExchangeDaoException {
        try {
            TypedQuery<ExchangeLog> query = em.createNamedQuery(ExchangeConstants.LOG_BY_TYPE_REF_AND_GUID, ExchangeLog.class);
            query.setParameter("typeRefGuid", typeRefGuid);
            query.setParameter("typeRefType", type);
            return query.getSingleResult();
        } catch (NoResultException e) {
            LOG.error("[ Error when getting entity by type ref ID. ] {}", e.getMessage());
            throw new NoEntityFoundException("[ Error when getting entity by type ref ID. ]");
        } catch (Exception e) {
            LOG.error("[ Error when getting entity by type ref ID. ] {}", e.getMessage());
            throw new ExchangeDaoException("[ Error when getting entity by type ref ID. ] ");
        }
	}
	
	@Override
	public ExchangeLog updateLog(ExchangeLog entity) throws ExchangeDaoException {
        try {
            em.merge(entity);
            em.flush();
            return entity;
        } catch (Exception e) {
            LOG.error("[ Error when updating entity ] {}", e.getMessage());
            throw new ExchangeDaoException("[ Error when updating entity ]", e);
        }
	}

}