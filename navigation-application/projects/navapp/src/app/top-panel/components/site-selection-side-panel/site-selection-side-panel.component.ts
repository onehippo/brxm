import { animate, style, transition, trigger } from '@angular/animations';
import { NestedTreeControl } from '@angular/cdk/tree';
import { Component, EventEmitter, HostBinding, Input, OnChanges, Output } from '@angular/core';
import { MatTreeNestedDataSource } from '@angular/material';

import { Site } from '../../../models';

@Component({
  selector: 'brna-site-selection-side-panel',
  templateUrl: 'site-selection-side-panel.component.html',
  styleUrls: ['site-selection-side-panel.component.scss'],
  animations: [
    trigger('slideInOut', [
      transition(':enter', [
        style({ transform: 'translateX(100%)' }),
        animate('300ms ease-in-out', style({ transform: 'translateX(0%)' })),
      ]),
      transition(':leave', [
        animate('300ms ease-in-out', style({ transform: 'translateX(100%)' })),
      ]),
    ]),
  ],
})
export class SiteSelectionSidePanelComponent implements OnChanges {
  @Input()
  sites: Site[];

  @Output()
  siteSelected = new EventEmitter<Site>();

  searchText = '';

  treeControl = new NestedTreeControl<Site>(node => node.subGroups);
  dataSource = new MatTreeNestedDataSource<Site>();

  @HostBinding('@slideInOut')
  animate = true;

  ngOnChanges(changes): void {
    this.dataSource.data = this.applyFilter(this.sites, this.searchText);
  }

  hasChild(index: number, node: Site): boolean {
    return node.subGroups && node.subGroups.length > 0;
  }

  onLeafNodeClicked(site: Site): void {
    this.siteSelected.emit(site);
  }

  onSearchInputKeyUp(): void {
    this.dataSource.data = this.applyFilter(this.sites, this.searchText);
  }

  private applyFilter(sites: Site[], searchText: string): Site[] {
    const predicate = site => {
      if (site.name.toLowerCase().includes(searchText.toLowerCase())) {
        return true;
      }

      if (site.subGroups) {
        site.subGroups = site.subGroups.slice().filter(predicate);
        return site.subGroups.length;
      }

      return false;
    };

    return sites.filter(predicate);
  }
}
