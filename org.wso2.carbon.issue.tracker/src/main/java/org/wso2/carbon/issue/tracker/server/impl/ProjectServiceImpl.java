package org.wso2.carbon.issue.tracker.server.impl;

import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.common.util.StringUtils;
import org.wso2.carbon.issue.tracker.bean.*;
import org.wso2.carbon.issue.tracker.dao.CommentDAO;
import org.wso2.carbon.issue.tracker.dao.IssueDAO;
import org.wso2.carbon.issue.tracker.dao.VersionDAO;
import org.wso2.carbon.issue.tracker.delegate.DAODelegate;
import org.wso2.carbon.issue.tracker.server.ProjectService;
import org.wso2.carbon.issue.tracker.util.IssueTrackerException;
import org.wso2.carbon.issue.tracker.util.TenantUtils;
import org.wso2.carbon.user.api.UserStoreException;

public class ProjectServiceImpl implements ProjectService {
    private static final Log log = LogFactory.getLog(ProjectServiceImpl.class);

    @Context
    private UriInfo ui;

    /**
     * @param tenantDomain
     * @return
     */
    @Override
    public Response getAllProject(String tenantDomain) {
        Response response = null;

        try {

            int tenantId = TenantUtils.getTenantId(tenantDomain);
            if (tenantId<=0) {
                throw new WebApplicationException(
                        new IllegalArgumentException(
                                "invalid organization id"));
            }

            List<Project> projects =
                    DAODelegate.getProjectInstance()
                            .getProjectsByOrganizationId(tenantId);

            GenericEntity<List<Project>> entity = new GenericEntity<List<Project>>(projects) {
            };
            response = Response.ok().entity(entity).build();

        } catch (SQLException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (UserStoreException use) {
            throw new WebApplicationException(use, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    @Override
    public Response getProject(String tenantDomain, int projectId) {
        Response response = null;
        try {
            Project project = DAODelegate.getProjectInstance().get(projectId);
            ResponseBean responseBean = new ResponseBean();

            if (project != null) {
                int tenantId = TenantUtils.getTenantId(tenantDomain);

                if (project.getOrganizationId() == tenantId) {
                   return Response.ok().entity(project).build();
                } else {
                    responseBean.setSuccess(false);
                    responseBean.setMessage("Invalid Tenant Domain");
                    return Response.status(Status.NOT_FOUND).build();
                }
            } else {
                responseBean.setSuccess(false);
                responseBean.setMessage("Invalid Project ID");
                return Response.status(Status.NOT_FOUND).entity(responseBean).build();
            }

        } catch (SQLException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (UserStoreException use) {
            throw new WebApplicationException(use, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Response addProject(String tenantDomain, Project project) {
        if (StringUtils.isEmpty(project.getName())) {
            throw new WebApplicationException(
                    new IllegalArgumentException(
                            "project name cannot be empty"));
        }

        if (StringUtils.isEmpty(project.getOwner())) {
            throw new WebApplicationException(
                    new IllegalArgumentException(
                            "project owner cannot be empty"));
        }
        try {
            int tenantId = TenantUtils.getTenantId(tenantDomain);

            if ((tenantId <= 0)) {
                throw new WebApplicationException(
                        new IllegalArgumentException(
                                "invalid organization id"));
            }
            project.setOrganizationId(tenantId);
            int projectId = DAODelegate.getProjectInstance().add(project);
            //response.setProjectId(projectId);
            String response = "id="+projectId;
            if(projectId>0){
                return Response.ok().entity(response).build();
            } else {
                return Response.notModified().entity(response).build();
            }

        } catch (SQLException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (UserStoreException use) {
            throw new WebApplicationException(use, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Response editProject(String tenantDomain, int projectId, Project project) {

        if (StringUtils.isEmpty(project.getName())) {
            throw new WebApplicationException(
                    new IllegalArgumentException(
                            "project name cannot be empty"));
        }

        if (StringUtils.isEmpty(project.getOwner())) {
            throw new WebApplicationException(
                    new IllegalArgumentException(
                            "project owner cannot be empty"));
        }

        ResponseBean response = new ResponseBean();

        try {
            int tenantId = TenantUtils.getTenantId(tenantDomain);
            if (tenantId<=0) {
                throw new WebApplicationException(
                        new IllegalArgumentException(
                                "invalid organization id"));
            }

            //check weather the project exists before proceeding.
            project.setId(projectId);
            if (DAODelegate.getProjectInstance().update(project)) {
                response.setSuccess(true);
                return Response.ok().entity(response).build();
            } else {
                response.setSuccess(false);
                response.setMessage("Data hasnt persist successfully");
                return Response.notModified().entity(response).build();
            }

        } catch (SQLException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (UserStoreException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    public Response getAllVersionsOfProject(String tenantDomain, int projectId) {

        Response response = null;

        try {

            int tenantId = TenantUtils.getTenantId(tenantDomain);
            if (tenantId<=0) {
                throw new WebApplicationException(
                        new IllegalArgumentException(
                                "invalid organization id"));
            }

            List<Version> versions =
                    DAODelegate.getVersionInstance()
                            .getVersionListOfProjectByProjectId(projectId);
            GenericEntity<List<Version>> entity = new GenericEntity<List<Version>>(versions) {
            };
            response = Response.ok().entity(entity).build();
        } catch (IssueTrackerException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (SQLException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (UserStoreException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return response;

    }

    @Override
    public Response getAllIssuesOfProject(String tenantDomain, int projectId) {
        Response response = null;
        try {
            int tenantId = TenantUtils.getTenantId(tenantDomain);
            if (tenantId<=0) {
                throw new WebApplicationException(
                        new IllegalArgumentException(
                                "invalid organization id"));
            }

            List<Issue> issues = DAODelegate.getIssueInstance().getAllIssuesOfProject(projectId);
            GenericEntity<List<Issue>> entity = new GenericEntity<List<Issue>>(issues) {};

            response = Response.ok().entity(entity).build();
        } catch (SQLException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (UserStoreException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    @Override
    public Response addNewIssueToProject(String tenantDomain, int projectId, Issue issue) {
        if (log.isDebugEnabled()) {
            log.debug("Executing addNewIssueToProject, created by: " + issue.getReporter());
        }
        if(issue.getProjectId()<=0 ){
            return Response.status(Response.Status.BAD_REQUEST).entity("Project ID cannot be empty!").build();
        }

        if (StringUtils.isEmpty(issue.getSummary())) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Issue summary cannot be empty!").build();
        }

        if (StringUtils.isEmpty(issue.getReporter())) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Issue owner cannot be empty!").build();
        }

        if(StringUtils.isEmpty(issue.getType())){
            return Response.status(Response.Status.BAD_REQUEST).entity("Issue Type cannot be empty!").build();
        }

        if(StringUtils.isEmpty(issue.getPriority())){
            issue.setPriority("NORMAL");
        }

        if(StringUtils.isEmpty(issue.getStatus())){
            issue.setStatus("OPEN");
        }

        if(StringUtils.isEmpty(issue.getType())){
            return Response.status(Response.Status.BAD_REQUEST).entity("Issue Type cannot be empty!").build();
        }

        if(StringUtils.isEmpty(issue.getType())){
            return Response.status(Response.Status.BAD_REQUEST).entity("Issue Type cannot be empty!").build();
        }

        IssueDAO issueDAO = DAODelegate.getIssueInstance();
        ResponseBean response = new ResponseBean();


        try {
            int tenantId = TenantUtils.getTenantId(tenantDomain);
            if (tenantId<=0) {
                throw new WebApplicationException(
                        new IllegalArgumentException(
                                "invalid organization id"));
            }

            String issueKey = issueDAO.add(issue);
            if (issueKey!=null){
                response.setSuccess(true);
                return Response.ok().entity(issueKey).type(MediaType.APPLICATION_JSON).build();
            } else{
                response.setSuccess(false);
                response.setMessage("Issue is not successfully inserted.");
                return Response.notModified().type(MediaType.APPLICATION_JSON_TYPE).entity(response).build();
            }
        } catch (SQLException e) {
            String msg = "Error while add Issue to Project, " + e.getMessage();
            log.error(msg, e);
            response.setSuccess(false);
            response.setMessage(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).type(MediaType.APPLICATION_JSON_TYPE).build();
        } catch (UserStoreException e) {
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }

    @Override
    public Response addNewVersionToProject(String tenantDomain, String projectKey, Version version) {
        if (log.isDebugEnabled()) {
            log.debug("Executing addNewVersionToProject, project versoin: " + version.getProjectVersion());
        }
        if(StringUtils.isEmpty(projectKey) ){
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Project!").build();
        }

        if (StringUtils.isEmpty(version.getProjectVersion())) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Project Version cannot be empty!").build();
        }

        //version.setProjectK(projectId);
        ResponseBean response = new ResponseBean();
        VersionDAO versionDAO = DAODelegate.getVersionInstance();
        try {

            int tenantId = TenantUtils.getTenantId(tenantDomain);
            if (tenantId<=0) {
                throw new WebApplicationException(
                        new IllegalArgumentException(
                                "invalid organization id"));
            }

            boolean isInserted = versionDAO.addVersionForProject(version, projectKey);
            response.setSuccess(isInserted);

            if (isInserted){
                return Response.ok().entity(response).type(MediaType.APPLICATION_JSON).build();
            } else {
                response.setMessage("Version is not successfully inserted.");
                return Response.notModified().type(MediaType.APPLICATION_JSON_TYPE).entity(response).build();
            }
        } catch (SQLException e) {
            String msg = "Error while add Version to Project, " + e.getMessage();
            response.setSuccess(false);
            response.setMessage(msg);
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).type(MediaType.APPLICATION_JSON_TYPE).build();
        } catch (UserStoreException e) {
            String msg = "Error while add Version to Project, " + e.getMessage();
            response.setSuccess(false);
            response.setMessage(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }
}
