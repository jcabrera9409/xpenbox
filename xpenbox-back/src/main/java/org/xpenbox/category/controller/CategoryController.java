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

/**
 * CategoryController handles HTTP requests related to category operations.
 * It provides endpoints for creating, updating, retrieving all categories,
 * and retrieving a specific category by its resource code.
 */
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

    /**
     * Create a new category
     * @param securityContext Security context of the authenticated user
     * @param categoryCreateDTO Data transfer object containing category creation details
     * @return Response containing the created category information
     */
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

    /**
     * Update an existing category
     * @param securityContext Security context of the authenticated user
     * @param resourceCode Unique resource code of the category to be updated
     * @param categoryUpdateDTO Data transfer object containing category update details
     * @return Response containing the updated category information
     */
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

    /**
     * Get all categories for the authenticated user
     * @param securityContext Security context of the authenticated user
     * @return Response containing the list of all categories
     */
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
    
    /**
     * Get a specific category by its resource code
     * @param securityContext Security context of the authenticated user
     * @param resourceCode Unique resource code of the category to be retrieved
     * @return Response containing the category information
     */
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
