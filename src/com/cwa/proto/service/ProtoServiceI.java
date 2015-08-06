package com.cwa.proto.service;

import serverice.proto.ChangeProtoInfo;
import serverice.proto.ProtoInfo;
import serverice.proto._IProtoServiceDisp;
import Ice.Current;

import com.cwa.proto.IPrototypeService;

/**
 * 原型服务
 * 
 * @author yangfeng
 *
 */
public class ProtoServiceI extends _IProtoServiceDisp {
	private static final long serialVersionUID = 1L;

	private IPrototypeService protoManager;

	@Override
	public ProtoInfo getPrototype(ChangeProtoInfo info, Current __current) {
		return protoManager.getPrototype(info);
	}

	@Override
	public ChangeProtoInfo checkPrototype(ChangeProtoInfo info, int iRegion, int iModule, Current __current) {
		return protoManager.checkPrototype(info, iRegion, iModule);
	}

	// --------------------------------------
	public void setProtoManager(IPrototypeService protoManager) {
		this.protoManager = protoManager;
	}
}
