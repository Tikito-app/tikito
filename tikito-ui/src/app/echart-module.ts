import {NgModule} from '@angular/core';
import { NgxEchartsModule } from 'ngx-echarts';
// import echarts core
import * as echarts from 'echarts/core';
// import necessary echarts components
import {BarChart, LineChart, PieChart} from 'echarts/charts';
import {DataZoomComponent, GridComponent, LegendComponent, TitleComponent, TooltipComponent} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
echarts.use([BarChart, GridComponent, CanvasRenderer, LineChart, TooltipComponent, LegendComponent, DataZoomComponent, PieChart, TitleComponent]);

@NgModule({
  imports: [
    NgxEchartsModule.forRoot({echarts}),
  ],
})
export class EChartModule {

}
