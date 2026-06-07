import {Component, OnInit} from '@angular/core';
import {JobApiService} from '../../api/job-api.service';
import {Job} from '../../dto/job';
import {CommonModule} from '@angular/common';
import {MatTableModule} from '@angular/material/table';
import {MatCardModule} from '@angular/material/card';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {TranslatePipe} from '../../service/translate-pipe.pipe';
import {CacheService} from "../../service/cache-service";
import {AuthService} from "../../service/auth.service";

@Component({
  selector: 'app-jobs-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatCardModule,
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
    TranslatePipe
  ],
  templateUrl: './admin-jobs-list.component.html',
  styleUrl: './admin-jobs-list.component.scss'
})
export class AdminJobsListComponent implements OnInit {
  jobs: Job[] = [];
  displayedColumns: string[] = ['timestamp', 'job-type', 'security-id', 'account-id', 'loan-id'];

  constructor(private jobApiService: JobApiService,
              private authService: AuthService) {
  }

  ngOnInit(): void {
    this.authService.onSystemReady(() => {
      this.loadJobs();
    });
  }

  loadJobs(): void {
    this.jobApiService.getJobs().subscribe(jobs => {
      this.jobs = jobs;
    });
  }

  protected readonly CacheService = CacheService;
}
