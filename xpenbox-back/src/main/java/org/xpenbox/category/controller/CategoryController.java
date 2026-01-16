package org.xpenbox.category.controller;

import org.jboss.logging.Logger;
import org.xpenbox.category.dto.CategoryCreateDTO;
import org.xpenbox.category.dto.CategoryResponseDTO;
import org.xpenbox.category.dto.CategoryUpdateDTO;
import org.xpenbox.category.service.ICategoryService;
import org.xpenbox.common.dto.APIResponseDTO;

import io.quarkus.security.Authenticated;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/category")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CategoryController {
    private static final Logger LOG = Logger.getLogger(CategoryController.class);

    private final ICategoryService categoryService;

    public CategoryController(
        ICategoryService categoryService
    ) {
        this.categoryService = categoryService;
    }

    @POST
    @Transactional
    public Response createCategory(@Context SecurityContext securityContext, @Valid CategoryCreateDTO categoryCreateDTO) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Create category request received for user: %s", userEmail);

        CategoryResponseDTO categoryResponse = categoryService.create(categoryCreateDTO, userEmail);
        LOG.infof("Category created successfully for user: %s", userEmail);

        return Response.status(Response.Status.CREATED).entity(
            APIResponseDTO.success("Category created successfully", categoryResponse, Response.Status.CREATED.getStatusCode())
        ).build();
    }

    @PUT
    @Path("/{resourceCode}")
    @Transactional
    public Response updateCategory(@Context SecurityContext securityContext, @PathParam("resourceCode") String resourceCode, @Valid CategoryUpdateDTO categoryUpdateDTO) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Update category request received for user: %s", userEmail);

        CategoryResponseDTO categoryResponse = categoryService.update(resourceCode, categoryUpdateDTO, userEmail);
        LOG.infof("Category updated successfully for user: %s", userEmail);

        return Response.status(Response.Status.OK).entity(
            APIResponseDTO.success("Category updated successfully", categoryResponse, Response.Status.OK.getStatusCode())
        ).build();
    }

    @GET
    public Response getAllCategories(@Context SecurityContext securityContext) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Get all categories request received for user: %s", userEmail);

        var categories = categoryService.getAll(userEmail);
        LOG.infof("Retrieved %d categories for user: %s", categories.size(), userEmail);

        return Response.status(Response.Status.OK).entity(
            APIResponseDTO.success("Categories retrieved successfully", categories, Response.Status.OK.getStatusCode())
        ).build();
    }
    
    @GET
    @Path("/{resourceCode}")
    public Response getCategoryByResourceCode(@Context SecurityContext securityContext, @PathParam("resourceCode") String resourceCode) {
        String userEmail = securityContext.getUserPrincipal().getName();
        LOG.infof("Get category by resourceCode request received for user: %s, resourceCode: %s", userEmail, resourceCode);

        CategoryResponseDTO categoryResponse = categoryService.getByResourceCode(resourceCode, userEmail);
        LOG.infof("Category retrieved successfully for user: %s, resourceCode: %s", userEmail, resourceCode);

        return Response.status(Response.Status.OK).entity(
            APIResponseDTO.success("Category retrieved successfully", categoryResponse, Response.Status.OK.getStatusCode())
        ).build();
    }
}
