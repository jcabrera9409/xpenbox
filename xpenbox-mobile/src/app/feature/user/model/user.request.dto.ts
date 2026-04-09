export class UserRequestDTO {
    email: string;
    password: string;
    currency: string;

    constructor(email: string, password: string, currency: string) {
        this.email = email;
        this.password = password;
        this.currency = currency;
    }
}