export class LogoutRequestDTO {
    fcmToken: string | null;

    constructor(fcmToken: string | null) {
        this.fcmToken = fcmToken;
    }
}