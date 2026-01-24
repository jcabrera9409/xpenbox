import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { ApiResponseDTO } from "../model/api.response.dto";
import { Observable } from "rxjs";

/**
 * Generic Service for managing entties, including creating, updating, and fetching entity data.
 * This service interacts with the backend API to perform CRUD operations on entities.
 */
@Injectable({
    providedIn: 'root'
})
export abstract class GenericService<TRequest, TResponse> {

    constructor(
        protected http: HttpClient,
        protected apiUrl: string
    ) { }

    /**
     * Creates a new entity.
     * @param t The data for the entity to be created.
     * @returns An observable of the API response containing the created entity.
     */
    create(t: TRequest): Observable<ApiResponseDTO<TResponse>> {
        return this.http.post<ApiResponseDTO<TResponse>>(this.apiUrl, t, { withCredentials: true });
    }

    /**
     * Updates an existing entity.
     * @param resourceCode The resource code of the entity to be updated.
     * @param t The updated data for the entity.
     * @returns An observable of the API response containing the updated entity.
     */
    update(resourceCode: string, t: TRequest): Observable<ApiResponseDTO<TResponse>> {
        return this.http.put<ApiResponseDTO<TResponse>>(`${this.apiUrl}/${resourceCode}`, t, { withCredentials: true });
    }

    /**
     * Fetches an entity by its resource code.
     * @param resourceCode The resource code of the entity to be fetched.
     * @returns An observable of the API response containing the requested entity.
     */
    getByResourceCode(resourceCode: string): Observable<ApiResponseDTO<TResponse>> {
        return this.http.get<ApiResponseDTO<TResponse>>(`${this.apiUrl}/${resourceCode}`, { withCredentials: true });
    }

    /**
     * Fetches all entities.
     * @returns An observable of the API response containing the list of entities.
     */
    getAll(): Observable<ApiResponseDTO<TResponse[]>> {
        return this.http.get<ApiResponseDTO<TResponse[]>>(this.apiUrl, { withCredentials: true });
    }

    /**
     * Refreshes the list of entities by reloading them from the backend.
     * This method is typically called after creating or updating an entity to ensure
     * the entity state reflects the latest data.
     */
    refresh(): void {
        this.load();
    }

    /**
     * Loads entities and updates the entity state with the fetched data.
     * Handles loading and error states.
     */
    abstract load(): void;
}