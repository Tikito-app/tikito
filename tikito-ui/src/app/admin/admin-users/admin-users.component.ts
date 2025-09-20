import {Component, OnInit, ViewChild} from '@angular/core';
import {NgIf} from "@angular/common";
import {
  MatCell,
  MatCellDef,
  MatColumnDef,
  MatHeaderCell,
  MatHeaderCellDef,
  MatHeaderRow,
  MatHeaderRowDef,
  MatRow,
  MatRowDef,
  MatTable,
  MatTableDataSource
} from "@angular/material/table";
import {PaginatorComponent} from "../../components/paginator/paginator.component";
import {TranslatePipe} from "@ngx-translate/core";
import {UserAccount} from "../../dto/user-account";
import {AuthService} from "../../service/auth.service";
import {AdminApi} from "../../api/admin-api";
import {MatSort} from "@angular/material/sort";
import {MatButton} from "@angular/material/button";
import {MatIcon} from "@angular/material/icon";
import {FormControl, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {LoggedInUser} from "../../dto/logged-in-user";
import {HttpRequestData} from "../../dto/http-request-data";
import {HttpService} from "../../service/http.service";
import {MatCard, MatCardContent, MatCardHeader, MatCardTitle} from "@angular/material/card";
import {MatError, MatFormField, MatLabel, MatSuffix} from "@angular/material/form-field";
import {MatInput} from "@angular/material/input";
import {Router} from "@angular/router";

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [
    MatCell,
    MatCellDef,
    TranslatePipe,
    MatColumnDef,
    MatHeaderCell,
    MatHeaderRow,
    MatHeaderRowDef,
    MatRow,
    MatRowDef,
    MatTable,
    NgIf,
    PaginatorComponent,
    TranslatePipe,
    MatHeaderCellDef,
    MatIcon,
    MatButton,
    MatCard,
    MatCardContent,
    MatCardHeader,
    MatCardTitle,
    MatError,
    MatFormField,
    MatInput,
    MatLabel,
    MatSuffix,
    ReactiveFormsModule
  ],
  providers: [TranslatePipe],
  templateUrl: './admin-users.component.html',
  styleUrl: './admin-users.component.scss'
})
export class AdminUsersComponent implements OnInit {
  displayedColumns: string[] = ['name'];
  dataSource: MatTableDataSource<UserAccount>;

  @ViewChild(PaginatorComponent) paginator: PaginatorComponent;
  @ViewChild(MatSort) sort: MatSort;

  userSelected: UserAccount | null;
  showPasswordFlag = false;
  form: FormGroup;
  errorMessage: string;


  constructor(
    private authService: AuthService,
    private http: HttpService,
    private router: Router,
    private api: AdminApi) {
  }

  ngOnInit() {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.api.getUsers().subscribe(users => {
        this.dataSource = new MatTableDataSource<UserAccount>(users);

        setTimeout(() => {
          this.dataSource.paginator = this.paginator.getPaginator();
          this.dataSource.sort = this.sort;
        });
      })
    });
  }

  onRowClicked(user: UserAccount) {
    this.userSelected = user;
    this.form = new FormGroup({
      email: new FormControl(''),
      password: new FormControl(''),
    });
    this.form.controls['email'].setValue(user.email);
  }

  onSaveClicked() {
    if (this.form.valid) {
      this.http.httpPostSingle<LoggedInUser>(
        LoggedInUser,
        new HttpRequestData()
          .withUrl('/api/admin/users/' + this.userSelected?.id)
          .withBody({
            'email': this.form.value.email,
            'password': this.form.value.password
          })).subscribe(updatedUser => {
        this.onCancelClicked();
      });
    }
  }

  onCancelClicked() {
    this.userSelected = null;
  }

  routeToAdmin() {
    this.router.navigate(['/admin']);
  }
}
