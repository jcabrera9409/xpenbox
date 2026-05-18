export class InputErrorDTO {
    requiredError: string | null = null;
    minLengthError: string | null = null;
    maxLengthError: string | null = null;
    minValueError: string | null = null;
    maxValueError: string | null = null;
}

export const defaultInputErrorDTO: Map<string, InputErrorDTO> = new Map<string, InputErrorDTO>([
    ['currentBalance', {
        requiredError: 'La línea utilizada es obligatoria.',
        minLengthError: null,
        maxLengthError: null,
        minValueError: 'La línea utilizada no puede ser menor a 0.',
        maxValueError: null
    }],
    ['billingDay', {
        requiredError: null,
        minLengthError: null,
        maxLengthError: null,
        minValueError: 'El día de facturación debe ser entre 1 y 28.',
        maxValueError: 'El día de facturación debe ser entre 1 y 28.'
    }],
    ['paymentDay', {
        requiredError: null,
        minLengthError: null,
        maxLengthError: null,
        minValueError: 'El día de pago debe ser entre 1 y 28.',
        maxValueError: 'El día de pago debe ser entre 1 y 28.'
    }],

    ['incomeDate', {
        requiredError: 'La fecha de ingreso es obligatoria.',
        minLengthError: null,
        maxLengthError: null,
        minValueError: null,
        maxValueError: null
    }],

    ['transactionDate', {
        requiredError: 'La fecha de la transacción es obligatoria.',
        minLengthError: null,
        maxLengthError: null,
        minValueError: null,
        maxValueError: null
    }]
]);