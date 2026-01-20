/**
 * Data Transfer Object for login requests.
 * Contains user credentials and a remember me option.
 */
export class LoginRequestDTO {
    email: string;
    password: string;
    rememberMe: boolean;
    
    constructor(email: string, password: string, rememberMe: boolean) {
        this.email = email;
        this.password = password;
        this.rememberMe = rememberMe;
    }
}