export class DeviceTokenRequestDTO {
    token: string | null;
    platform: string | null;

    constructor(token: string | null, platform: string | null) {
        this.token = token;
        this.platform = platform;
    }
}