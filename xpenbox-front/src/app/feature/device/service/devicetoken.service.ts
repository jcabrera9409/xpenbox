import { Injectable } from '@angular/core';
import { GenericService } from '../../common/service/generic.service';
import { DeviceTokenRequestDTO } from '../model/devicetoken.request.dto';
import { HttpClient } from '@angular/common/http';
import { EnvService } from '../../common/service/env.service';

@Injectable({
  providedIn: 'root',
})
export class DevicetokenService extends GenericService<DeviceTokenRequestDTO, null>  {
  
  constructor(
    protected override http: HttpClient,
    protected envService: EnvService
  ) {
    super ( 
      http,
      `${envService.getApiUrl()}/device`
    )
  }

  
  /** @deprecated No need to load device tokens as they are managed individually when registered or unregistered */
  override load(): void {
    // No need to load device tokens as they are managed individually when registered or unregistered
  }
  
}
