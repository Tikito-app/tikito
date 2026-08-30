import {Routes} from '@angular/router';
import {
  MoneyTransactionGroupListComponent
} from "./money/money-transaction-group-list/money-transaction-group-list.component";
import {
  MoneyTransactionGroupFormComponent
} from "./money/money-transaction-group-form/money-transaction-group-form.component";
import {
  MoneyTransactionOverviewComponent
} from "./money/money-transaction-overview/money-transaction-overview.component";
import {AccountListComponent} from "./account/account-list/account-list.component";
import {AccountFormComponent} from "./account/account-form/account-form.component";
import {
  SecurityHoldingOverviewComponent
} from "./security/security-holding-overview/security-holding-overview.component";
import {ImportComponent} from "./account/import/import.component";
import {LoginComponent} from "./user/login/login.component";
import {RegisterComponent} from "./user/register/register.component";
import {LogoutComponent} from "./user/logout/logout.component";
import {SecurityHoldingListComponent} from "./security/security-holding-list/security-holding-list.component";
import {OverviewComponent} from "./overview/overview.component";
import {InitialInstallationComponent} from "./initial-installation/initial-installation.component";
import {AdminComponent} from "./admin/admin/admin.component";
import {AdminUsersComponent} from "./admin/admin-users/admin-users.component";
import {AdminSecurityListComponent} from "./admin/security/admin-security-list/admin-security-list.component";
import {AdminSecurityFormComponent} from "./admin/security/admin-security-form/admin-security-form.component";
import {
  AdminSecurityIsinListComponent
} from "./admin/security/admin-security-isin-list/admin-security-isin-list.component";
import {
  AdminSecurityIsinFormComponent
} from "./admin/security/admin-security-isin-form/admin-security-isin-form.component";
import {AdminMoneyListComponent} from "./admin/money/admin-money-list/admin-money-list.component";
import {LoanPartFormComponent} from "./loan/loan-part-form/loan-part-form.component";
import {LoanFormComponent} from "./loan/loan-form/loan-form.component";
import {LoanListComponent} from "./loan/loan-list/loan-list.component";
import {AdminUserPreferencesComponent} from "./admin/admin-user-preferences/admin-user-preferences.component";
import {AdminExportComponent} from "./admin/admin-export/admin-export.component";
import {AdminImportComponent} from "./admin/admin-import/admin-import.component";
import { LogsListComponent } from './admin/logs-list/logs-list.component';
import {AdminJobsListComponent} from "./admin/jobs-list/admin-jobs-list.component";

export const routes: Routes = [
  {path: '', component: OverviewComponent, data: {menu: 'home'}},

  {path: 'login', component: LoginComponent, data: {menu: 'login'}},
  {path: 'logout', component: LogoutComponent},
  {path: 'register', component: RegisterComponent},

  {path: 'account', component: AccountListComponent, data: {menu: 'account'}},
  {path: 'account/create', component: AccountFormComponent, data: {menu: 'account'}},
  {path: 'account/import', component: ImportComponent, data: {menu: 'import'}},
  {path: 'account/:accountId', component: AccountFormComponent, data: {menu: 'account'}},

  {path: 'money-transaction', component: MoneyTransactionOverviewComponent, data: {menu: 'money'}},

  {path: 'security', component: SecurityHoldingListComponent, data: {menu: 'securities'}},

  {path: 'security-holding', component: SecurityHoldingOverviewComponent, data: {menu: 'securities'}},

  {path: 'money/transaction-group', component: MoneyTransactionGroupListComponent, data: {menu: 'money'}},
  {path: 'money/transaction-group/:groupId', component: MoneyTransactionGroupFormComponent, data: {menu: 'money'}},

  {path: 'loan', component: LoanListComponent, data: {menu: 'loan'}},
  {path: 'loan/:loanId', component: LoanFormComponent, data: {menu: 'loan'}},
  {path: 'loan/:loanId/part', component: LoanPartFormComponent, data: {menu: 'loan'}},
  {path: 'loan/:loanId/part/:loanPartId', component: LoanPartFormComponent, data: {menu: 'loan'}},

  {path: 'admin', component: AdminComponent, data: {menu: 'admin'}},
  {path: 'admin/user-preferences', component: AdminUserPreferencesComponent, data: {menu: 'admin'}},
  {path: 'admin/users', component: AdminUsersComponent, data: {menu: 'admin'}},
  {path: 'admin/money', component: AdminMoneyListComponent, data: {menu: 'admin'}},
  {path: 'admin/export', component: AdminExportComponent, data: {menu: 'admin'}},
  {path: 'admin/import', component: AdminImportComponent, data: {menu: 'admin'}},
  {path: 'admin/jobs', component: AdminJobsListComponent, data: {menu: 'admin'}},
  {path: 'admin/logs', component: LogsListComponent, data: {menu: 'admin'}},

  {path: 'admin/security', component: AdminSecurityListComponent, data: {menu: 'admin'}},
  {path: 'admin/security/:securityId', component: AdminSecurityFormComponent, data: {menu: 'admin'}},
  {path: 'admin/security/:securityId/isin', component: AdminSecurityIsinListComponent, data: {menu: 'admin'}},
  {path: 'admin/security/:securityId/isin/:isin', component: AdminSecurityIsinFormComponent, data: {menu: 'admin'}},

  {path: 'initial-installation', component: InitialInstallationComponent},
];
