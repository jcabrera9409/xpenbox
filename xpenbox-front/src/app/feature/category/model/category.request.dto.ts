/**
 * DTO representing the request structure for creating/updating a category.
 */
export class CategoryRequestDTO {
    name: string;
    color: string;
    state: boolean;
    budget: number;
    hasBudget: boolean;

    constructor(name: string, color: string, hasBudget: boolean, budget: number, state: boolean = true) {
        this.name = name;
        this.color = color;
        this.state = state;
        this.budget = budget; 
        this.hasBudget = hasBudget; 
    }
}
