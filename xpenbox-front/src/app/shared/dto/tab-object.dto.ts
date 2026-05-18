export class TabObjectDTO {
    id: string;
    label: string;
    iconName: string;

    constructor(id: string, label: string, iconName: string) {
        this.id = id;
        this.label = label;
        this.iconName = iconName;
    }
}