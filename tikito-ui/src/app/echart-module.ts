import {NgModule} from '@angular/core';
import { NgxEchartsModule } from 'ngx-echarts';
import * as echarts from 'echarts/core';
import {BarChart, LineChart, PieChart} from 'echarts/charts';
import {
  DataZoomComponent,
  GridComponent,
  LegendComponent,
  TitleComponent,
  TooltipComponent,
  VisualMapComponent
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
echarts.use([BarChart, GridComponent, CanvasRenderer, LineChart, TooltipComponent, LegendComponent, DataZoomComponent, PieChart, TitleComponent, VisualMapComponent]);

@NgModule({
  imports: [
    NgxEchartsModule.forRoot({echarts}),
  ],
})
export class EChartModule {

}
