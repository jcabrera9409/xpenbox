/**
 * DTO representing the request structure for creating/updating a category.
 */
export class CategoryRequestDTO {
    name: string;
    color: string;
    state: boolean;

    constructor(name: string, color: string, state: boolean = true) {
        this.name = name;
        this.color = color;
        this.state = state;
    }
}
