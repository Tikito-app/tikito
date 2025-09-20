import 'angular-server-side-configuration/process';

export const environment = {
  production: true,
  hostname: process.env['TIKITO_API_HOSTNAME'] + ':' + process.env['TIKIT_API_PORT'],
};
