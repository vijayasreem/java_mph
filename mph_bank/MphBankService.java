package com.lic.epgs.mst.usermgmt.service;

import java.util.List;

import org.json.JSONObject;
import org.springframework.http.ResponseEntity;

import com.lic.epgs.mst.usermgmt.entity.MasterPolicyDataEntity;
import com.lic.epgs.mst.usermgmt.entity.MasterRolesBulkEntity;
import com.lic.epgs.mst.usermgmt.entity.MphNameEntity;
import com.lic.epgs.mst.usermgmt.entity.PortalMasterEntity;

public interface MphBankService {
	
	public List<MphNameEntity> searchBankDetails(String bankName) throws Exception;
	
	public JSONObject getAllBankDetails(String bankName) throws Exception;
	
	public List<MphNameEntity> getAllBankNames(String isActive) throws Exception;
	
	public boolean deleteAllBankDetails(String bankName, String token) throws Exception;
	
	public boolean checkBankAvailability(String bankName) throws Exception;
	
	public JSONObject saveFirstAdminDetailsInDBAndRHSSO(JSONObject adminUserObj, String token) throws Exception;
	
	public boolean deleteGroup(String groupId, String token) throws Exception;
	
	public boolean checkUserSuperAdminOrNot(String username) throws Exception;
	
	public List<PortalMasterEntity> getAllActiveInactiveAdminOrdinaryUsers(String is_MphAdmin, String is_Active) throws Exception;
	
	public PortalMasterEntity getUserDetails(String username) throws Exception;
	
	public List<MasterRolesBulkEntity> getUnAssignedPortalsUsingMPHName(String mphName) throws Exception;
	
	public List<MasterRolesBulkEntity> getAssignedPortalsUsingMPHName(String mphName) throws Exception;
	
	public boolean deleteAssignedPortalsUsingMPHName(String mphName, String roleKey) throws Exception;
	
	public boolean assignPortalsUsingMPHName(String mphName, String roleKey) throws Exception;
	
	//public ResponseEntity<Object> checkPolicyAvailability(String policyNumber, String loggedInUserUnitCode) throws Exception;
	
	public MphNameEntity getMphnameBasedOnPolicyNumber(String policyNumber) throws Exception;
	
	public ResponseEntity<Object> addPolicyDetails(MasterPolicyDataEntity masterPolicyDataEntity)
			throws Exception;

}