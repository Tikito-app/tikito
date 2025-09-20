import {Routes} from '@angular/router';
import {BudgetFormComponent} from "./budget/budget-form/budget-form.component";
import {BudgetListComponent} from "./budget/budget-list/budget-list.component";
import {
  MoneyTransactionGroupListComponent
} from "./money/money-transaction-group-list/money-transaction-group-list.component";
import {
  MoneyTransactionGroupFormComponent
} from "./money/money-transaction-group-form/money-transaction-group-form.component";
import {MoneyTransactionOverviewComponent} from "./money/money-transaction-overview/money-transaction-overview.component";
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

export const routes: Routes = [
  {path: '', component: OverviewComponent},

  {path: 'login', component: LoginComponent},
  {path: 'logout', component: LogoutComponent},
  {path: 'register', component: RegisterComponent},

  {path: 'budget', component: BudgetListComponent},
  {path: 'budget/create', component: BudgetFormComponent},
  {path: 'budget/:budgetId', component: BudgetFormComponent},

  {path: 'account', component: AccountListComponent},
  {path: 'account/create', component: AccountFormComponent},
  {path: 'account/import', component: ImportComponent},
  {path: 'account/:accountId', component: AccountFormComponent},

  {path: 'money-transaction', component: MoneyTransactionOverviewComponent},

  {path: 'security', component: SecurityHoldingListComponent},

  {path: 'security-holding', component: SecurityHoldingOverviewComponent},

  {path: 'money/transaction-group', component: MoneyTransactionGroupListComponent},
  {path: 'money/transaction-group/:groupId', component: MoneyTransactionGroupFormComponent},

  {path: 'admin', component: AdminComponent},
  {path: 'admin/users', component: AdminUsersComponent},
  {path: 'admin/money', component: AdminMoneyListComponent},

  {path: 'admin/security', component: AdminSecurityListComponent},
  {path: 'admin/security/:securityId', component: AdminSecurityFormComponent},
  {path: 'admin/security/:securityId/isin', component: AdminSecurityIsinListComponent},
  {path: 'admin/security/:securityId/isin/:isin', component: AdminSecurityIsinFormComponent},

  {path: 'initial-installation', component: InitialInstallationComponent},
];
