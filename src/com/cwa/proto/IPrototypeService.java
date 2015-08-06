package com.cwa.proto;

import serverice.proto.ChangeProtoInfo;
import serverice.proto.ProtoInfo;

import com.cwa.service.IModuleServer;

public interface IPrototypeService extends IModuleServer {
	ProtoInfo getPrototype(ChangeProtoInfo info);

	ChangeProtoInfo checkPrototype(ChangeProtoInfo info, int iRegion, int iModule);
}
