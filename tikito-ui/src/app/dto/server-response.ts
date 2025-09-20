export class ServerResponse {
    static ResourceNotFoundException = 'ResourceNotFoundException';
    static ResourceAlreadyExistsException = 'ResourceAlreadyExistsException';
    static InvalidCredentialsException = 'InvalidCredentialsException';
    static GroupNotEmptyException = 'GroupNotEmptyException';
    static InvalidContactException = 'InvalidContactException';

    error: string;
    message: string;
    data: any;
}
