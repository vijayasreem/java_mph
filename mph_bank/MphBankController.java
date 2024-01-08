package com.lic.epgs.mst.usermgmt.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lic.epgs.constant.Constant;
import com.lic.epgs.mst.entity.UnitOffice;
import com.lic.epgs.mst.entity.ZonalEntity;
import com.lic.epgs.mst.modal.MasterPolicyDataModel;
import com.lic.epgs.mst.repository.UnitCodeRepository;
import com.lic.epgs.mst.repository.UnitOfficeRepository;
import com.lic.epgs.mst.repository.ZonalEntityRepository;
import com.lic.epgs.mst.usermgmt.encryptdecrypt.EncryptandDecryptAES;
import com.lic.epgs.mst.usermgmt.encryptdecrypt.EncryptionModel;
import com.lic.epgs.mst.usermgmt.entity.MasterPolicyDataEntity;
import com.lic.epgs.mst.usermgmt.entity.MasterRolesBulkEntity;
import com.lic.epgs.mst.usermgmt.entity.MphNameEntity;
import com.lic.epgs.mst.usermgmt.entity.PortalMasterEntity;
import com.lic.epgs.mst.usermgmt.exceptionhandling.MphBankServiceException;
import com.lic.epgs.mst.usermgmt.service.MphBankService;

@CrossOrigin("*")
@RestController
public class MphBankController {

	private static final Logger logger = LoggerFactory.getLogger(MphBankController.class);
	
	String className = this.getClass().getSimpleName();
	
	@Autowired
	private MphBankService mphBankService;
	
	@Autowired
	private EncryptandDecryptAES encryptandDecryptAES;
	
	@Autowired
	UnitOfficeRepository unitOfficeRepository;
	
	@Autowired
	ZonalEntityRepository zonalEntityRepository;
	
	@PostMapping(value = "/usermgmt/searchBank")
	public ResponseEntity<Object> searchBank(@RequestHeader("Authorization") String token, @RequestBody EncryptionModel encryptionModel) throws MphBankServiceException 
	{
		logger.info("MphBankController : searchBank : initiated");

		Map<String, Object> response = new HashMap<String, Object>(4);
		response.put(Constant.STATUS, 0);
		response.put(Constant.MESSAGE, Constant.SOMETHING_WENT_WRONG);
		
		Map<String, Object> response1 = new HashMap<String, Object>();
		logger.info("encryptionModel::"+encryptionModel.getEncryptedPayload());
		ObjectMapper Obj = new ObjectMapper(); 
		try {
			JSONObject plainJSONObject = EncryptandDecryptAES.DecryptAESECBPKCS5Padding(encryptionModel.getEncryptedPayload());

			List<MphNameEntity> mphNameEntities = mphBankService.searchBankDetails(plainJSONObject.getString("bankName"));
			if(mphNameEntities == null || mphNameEntities.size() == 0) 
			{
				response.put(Constant.STATUS, 201);
				response.put(Constant.MESSAGE, Constant.NO_DATA_FOUND);
				
				String jsonStr = Obj.writeValueAsString(response); 
				
				//ENcryption Technique
				String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
				response1.put(Constant.STATUS, 201);                 
				response1.put(Constant.MESSAGE, Constant.NO_DATA_FOUND);                
				response1.put("body", encaccessResponse); 
				return new ResponseEntity <Object>(response1,HttpStatus.UNAUTHORIZED);
			
			} 
			else 
			{
				response.put(Constant.STATUS, 200);
				response.put(Constant.MESSAGE, Constant.SUCCESS);
				response.put("Data", mphNameEntities);
				
				String jsonStr = Obj.writeValueAsString(response); 
				
				//ENcryption Technique
				String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
				
				response1.put(Constant.STATUS, 200);                 
				response1.put(Constant.MESSAGE, Constant.SUCCESS);                
				response1.put("body", encaccessResponse); 
				return new ResponseEntity<Object>(response1, HttpStatus.OK);
			}

		} catch (Exception e) {
			logger.error("searchBank exception occured." + e.getMessage());
			throw new MphBankServiceException("Internal Server Error");
		}
	}
	
	@PostMapping(value = "/usermgmt/getAllBankDetails")
	public ResponseEntity<Object> getAllBankDetails(@RequestHeader("Authorization") String token, @RequestBody EncryptionModel encryptionModel) throws MphBankServiceException 
	{
		logger.info("MphBankController : getAllBankDetails : initiated");

		Map<String, Object> response = new HashMap<String, Object>(4);
		response.put(Constant.STATUS, 0);
		response.put(Constant.MESSAGE, Constant.SOMETHING_WENT_WRONG);
		
		Map<String, Object> response1 = new HashMap<String, Object>();
		logger.info("encryptionModel::"+encryptionModel.getEncryptedPayload());
		ObjectMapper Obj = new ObjectMapper(); 
		try {
			JSONObject plainJSONObject = EncryptandDecryptAES.DecryptAESECBPKCS5Padding(encryptionModel.getEncryptedPayload());

			JSONObject bankDetailsObject = mphBankService.getAllBankDetails(plainJSONObject.getString("bankName"));
			if(bankDetailsObject == null) 
			{
				response.put(Constant.STATUS, 201);
				response.put(Constant.MESSAGE, Constant.NO_DATA_FOUND);
				
				String jsonStr = Obj.writeValueAsString(response); 
				
				//ENcryption Technique
				String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
				response1.put(Constant.STATUS, 201);                 
				response1.put(Constant.MESSAGE, Constant.NO_DATA_FOUND);                
				response1.put("body", encaccessResponse); 
				return new ResponseEntity <Object>(response1,HttpStatus.OK);
			
			} 
			else 
			{
				response.put(Constant.STATUS, 200);
				response.put(Constant.MESSAGE, Constant.SUCCESS);
				response.put("Data", bankDetailsObject.toMap());
				
				String jsonStr = Obj.writeValueAsString(response); 
				
				//ENcryption Technique
				String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
				
				response1.put(Constant.STATUS, 200);                 
				response1.put(Constant.MESSAGE, Constant.SUCCESS);                
				response1.put("body", encaccessResponse); 
				return new ResponseEntity<Object>(response1, HttpStatus.OK);
			}

		} catch (Exception e) {
			logger.error("searchBank exception occured." + e.getMessage());
			throw new MphBankServiceException("Internal Server Error");
		}
	}
	
	@GetMapping(value = "/usermgmt/getAllBankNames/{isActive}")
	public ResponseEntity<Object> getAllBankNames(@RequestHeader("Authorization") String token, @PathVariable("isActive") String isActive) throws MphBankServiceException 
	{
		Map<String, Object> response = new HashMap<String, Object>(4);
		response.put(Constant.STATUS, 0);
		response.put(Constant.MESSAGE, Constant.SOMETHING_WENT_WRONG);
		
		Map<String, Object> response1 = new HashMap<String, Object>();
		ObjectMapper Obj = new ObjectMapper();
			try {
				List<MphNameEntity> mphNameEntities = mphBankService.getAllBankNames(isActive);
				if(mphNameEntities == null || mphNameEntities.size() == 0) 
				{
					response.put(Constant.STATUS, 201);
					response.put(Constant.MESSAGE, Constant.NO_DATA_FOUND);
					
					String jsonStr = Obj.writeValueAsString(response); 
					
					//ENcryption Technique
					String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
					response1.put(Constant.STATUS, 200);                 
					response1.put(Constant.MESSAGE, Constant.SUCCESS);                
					response1.put("body", encaccessResponse); 
					return new ResponseEntity <Object>(response1,HttpStatus.OK);
				
				} 
				else 
				{
					response.put(Constant.STATUS, 200);
					response.put(Constant.MESSAGE, Constant.SUCCESS);
					response.put("Data", mphNameEntities);
					
					String jsonStr = Obj.writeValueAsString(response); 
					
					//ENcryption Technique
					String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
					
					response1.put(Constant.STATUS, 200);                 
					response1.put(Constant.MESSAGE, Constant.SUCCESS);                
					response1.put("body", encaccessResponse); 
					return new ResponseEntity<Object>(response1, HttpStatus.OK);
				}
			} catch (Exception e) {
				logger.error(" update Assign roles by username exception occured." + e.getMessage());
				throw new MphBankServiceException("Internal Server Error");
			}
	}
	
	@PostMapping(value = "/usermgmt/deleteAllBankDetails")
	public ResponseEntity<Object> deleteAllBankDetails(@RequestHeader("Authorization") String token, @RequestBody EncryptionModel encryptionModel) throws MphBankServiceException 
	{
		logger.info("MphBankController : deleteAllBankDetails : initiated");

		Map<String, Object> response = new HashMap<String, Object>(4);
		response.put(Constant.STATUS, 0);
		response.put(Constant.MESSAGE, Constant.SOMETHING_WENT_WRONG);
		
		Map<String, Object> response1 = new HashMap<String, Object>();
		logger.info("encryptionModel::"+encryptionModel.getEncryptedPayload());
		ObjectMapper Obj = new ObjectMapper(); 
		try {
			JSONObject plainJSONObject = EncryptandDecryptAES.DecryptAESECBPKCS5Padding(encryptionModel.getEncryptedPayload());

			boolean bankDetailsObject = mphBankService.deleteAllBankDetails(plainJSONObject.getString("bankName"), token);
			if(!bankDetailsObject) 
			{
				response.put(Constant.STATUS, 201);
				response.put(Constant.MESSAGE, Constant.NO_DATA_FOUND);
				
				String jsonStr = Obj.writeValueAsString(response); 
				
				//ENcryption Technique
				String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
				response1.put(Constant.STATUS, 201);                 
				response1.put(Constant.MESSAGE, Constant.NO_DATA_FOUND);                
				response1.put("body", encaccessResponse); 
				return new ResponseEntity <Object>(response1,HttpStatus.OK);
			
			} 
			else 
			{
				response.put(Constant.STATUS, 200);
				response.put(Constant.MESSAGE, Constant.SUCCESS);
				response.put("Data", bankDetailsObject);
				
				String jsonStr = Obj.writeValueAsString(response); 
				
				//ENcryption Technique
				String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
				
				response1.put(Constant.STATUS, 200);                 
				response1.put(Constant.MESSAGE, Constant.SUCCESS);                
				response1.put("body", encaccessResponse); 
				return new ResponseEntity<Object>(response1, HttpStatus.OK);
			}

		} catch (Exception e) {
			logger.error("deleteAllBankDetails exception occured." + e.getMessage());
			throw new MphBankServiceException("Internal Server Error");
		}
	}
	
	@PostMapping(value = "/usermgmt/checkBankAvailability")
	public ResponseEntity<Object> checkBankAvailability(@RequestHeader("Authorization") String token, @RequestBody EncryptionModel encryptionModel) throws MphBankServiceException 
	{
		logger.info("MphBankController : checkBankAvailability : initiated");

		Map<String, Object> response = new HashMap<String, Object>(4);
		response.put(Constant.STATUS, 0);
		response.put(Constant.MESSAGE, Constant.SOMETHING_WENT_WRONG);
		
		Map<String, Object> response1 = new HashMap<String, Object>();
		logger.info("encryptionModel::"+encryptionModel.getEncryptedPayload());
		ObjectMapper Obj = new ObjectMapper(); 
		try {
			JSONObject plainJSONObject = EncryptandDecryptAES.DecryptAESECBPKCS5Padding(encryptionModel.getEncryptedPayload());

			boolean bankDetailsObject = mphBankService.checkBankAvailability(plainJSONObject.getString("bankName"));
			if(!bankDetailsObject) 
			{
				response.put(Constant.STATUS, 200);
				response.put(Constant.MESSAGE, Constant.SUCCESS);
				
				String jsonStr = Obj.writeValueAsString(response); 
				
				//ENcryption Technique
				String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
				response1.put(Constant.STATUS, 200);                 
				response1.put(Constant.MESSAGE, Constant.SUCCESS);                
				response1.put("body", encaccessResponse); 
				return new ResponseEntity <Object>(response1,HttpStatus.OK);
			
			} 
			else 
			{
				response.put(Constant.STATUS, 201);
				response.put(Constant.MESSAGE, Constant.DUPLICATE_ENTRY);
				
				String jsonStr = Obj.writeValueAsString(response); 
				
				//ENcryption Technique
				String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
				
				response1.put(Constant.STATUS, 201);                 
				response1.put(Constant.MESSAGE, Constant.DUPLICATE_ENTRY);                
				response1.put("body", encaccessResponse); 
				return new ResponseEntity<Object>(response1, HttpStatus.OK);
			}

		} catch (Exception e) {
			logger.error("checkBankAvailability exception occured." + e.getMessage());
			throw new MphBankServiceException("Internal Server Error");
		}
	}
	
	
//	@PostMapping(value = "/usermgmt/checkPolicyAvailability")
//	public ResponseEntity<Object> checkPolicyAvailability(@RequestBody EncryptionModel encryptionModel) throws MphBankServiceException {
//
//		try {
//			JSONObject plainJSONObject = EncryptandDecryptAES.DecryptAESECBPKCS5Padding(encryptionModel.getEncryptedPayload());
//			return  mphBankService.checkPolicyAvailability(plainJSONObject.getString("policyNumber"),plainJSONObject.getString("loggedInUserUnitCode"));
//		} catch (Exception e) {
//			logger.error(" insertUserDataAtLoginTime exception occured." + e.getMessage());
//			throw new MphBankServiceException ("Internal Server Error");
//		}
//	}
	
	@PostMapping(value = "/usermgmt/addPolicyDetails")
	public ResponseEntity<Object> addPolicyDetails(@RequestBody MasterPolicyDataModel masterPolicyDataModel) throws  MphBankServiceException {

		try {
			Date today = new Date();
			MasterPolicyDataEntity masterPolicyDataEntity = new MasterPolicyDataEntity();			
			masterPolicyDataEntity.setPolicyNumber(masterPolicyDataModel.getPolicyNumber());
			masterPolicyDataEntity.setPolicyId(masterPolicyDataModel.getPolicyId());
			UnitOffice unitOffice = unitOfficeRepository.findByUnitOfficeCode(masterPolicyDataModel.getUnitCode());
			if(unitOffice != null) {
				masterPolicyDataEntity.setUnitId(unitOffice.getUnitId());
				masterPolicyDataEntity.setUnitCode(masterPolicyDataModel.getUnitCode());
			}			
			ZonalEntity zonalEntity = zonalEntityRepository.getAllZoneDetailsByUnitCode(masterPolicyDataModel.getUnitCode());
			if(zonalEntity != null) {
				masterPolicyDataEntity.setZoneId(zonalEntity.getZonalId());
				masterPolicyDataEntity.setZoneName(zonalEntity.getDescription());
			}			
			masterPolicyDataEntity.setCreatedBy(masterPolicyDataModel.getCreatedBy());
			masterPolicyDataEntity.setCreatedOn(today);
			masterPolicyDataEntity.setModifiedBy(null);
			masterPolicyDataEntity.setModifiedOn(null);
			masterPolicyDataEntity.setIsActive("Y");
			masterPolicyDataEntity.setStream(masterPolicyDataModel.getStream());
			masterPolicyDataEntity.setMphCode(masterPolicyDataModel.getMphCode());
			masterPolicyDataEntity.setMphName(masterPolicyDataModel.getMphName());		
			
			return  mphBankService.addPolicyDetails(masterPolicyDataEntity);
		} catch (Exception e) {
			logger.error(" add master Policy exception occured." + e.getMessage());
			throw new MphBankServiceException ("Internal Server Error");
		}
	}
	
	@PostMapping(value = "/usermgmt/saveFirstAdminDetailsInDBAndRHSSO")
	public ResponseEntity<Object> saveFirstAdminDetailsInDBAndRHSSO(@RequestHeader("Authorization") String token, @RequestBody EncryptionModel encryptionModel) throws MphBankServiceException 
	{
		logger.info("MphBankController : saveFirstAdminDetailsInDBAndRHSSO : initiated");

		Map<String, Object> response = new HashMap<String, Object>(4);
		response.put(Constant.STATUS, 0);
		response.put(Constant.MESSAGE, Constant.SOMETHING_WENT_WRONG);
		
		Map<String, Object> response1 = new HashMap<String, Object>();
		logger.info("encryptionModel::"+encryptionModel.getEncryptedPayload());
		ObjectMapper Obj = new ObjectMapper(); 
		try {
			JSONObject plainJSONObject = EncryptandDecryptAES.DecryptAESECBPKCS5Padding(encryptionModel.getEncryptedPayload());

			
			JSONObject firstAdminAddedResponse = mphBankService.saveFirstAdminDetailsInDBAndRHSSO(plainJSONObject, token);
			
			if(firstAdminAddedResponse == null) 
			{
				response.put(Constant.STATUS, 201);
				response.put(Constant.MESSAGE, "User Not Added");
				
				String jsonStr = Obj.writeValueAsString(response); 
				
				//ENcryption Technique
				String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
				response1.put(Constant.STATUS, 201);                 
				response1.put(Constant.MESSAGE, Constant.NO_DATA_FOUND);                
				response1.put("body", encaccessResponse); 
				return new ResponseEntity <Object>(response1,HttpStatus.UNAUTHORIZED);
			
			} 
			else 
			{
				response.put(Constant.STATUS, 200);
				response.put(Constant.MESSAGE, "User Added Successfully");
				response.put("Data", firstAdminAddedResponse.toMap());
				
				String jsonStr = Obj.writeValueAsString(response); 
				
				//ENcryption Technique
				String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
				
				response1.put(Constant.STATUS, 200);                 
				response1.put(Constant.MESSAGE, Constant.SUCCESS);                
				response1.put("body", encaccessResponse); 
				return new ResponseEntity<Object>(response1, HttpStatus.OK);
			}

		} catch (Exception e) {
			logger.error("saveFirstAdminDetailsInDBAndRHSSO exception occured." + e.getMessage());
			throw new MphBankServiceException("Internal Server Error");
		}
	}
	
	@PostMapping(value = "/usermgmt/checkUserSuperAdminOrNot")
	public ResponseEntity<Object> checkUserSuperAdminOrNot(@RequestHeader("Authorization") String token, @RequestBody EncryptionModel encryptionModel) throws MphBankServiceException 
	{
		logger.info("MphBankController : checkUserSuperAdminOrNot : initiated");

		Map<String, Object> response = new HashMap<String, Object>(4);
		response.put(Constant.STATUS, 0);
		response.put(Constant.MESSAGE, Constant.SOMETHING_WENT_WRONG);
		
		Map<String, Object> response1 = new HashMap<String, Object>();
		logger.info("encryptionModel::"+encryptionModel.getEncryptedPayload());
		ObjectMapper Obj = new ObjectMapper(); 
		try {
			JSONObject plainJSONObject = EncryptandDecryptAES.DecryptAESECBPKCS5Padding(encryptionModel.getEncryptedPayload());

			boolean superAdminuserOrNot = mphBankService.checkUserSuperAdminOrNot(plainJSONObject.getString("username"));
			if(superAdminuserOrNot) 
			{
				response.put(Constant.STATUS, 200);
				response.put(Constant.MESSAGE, Constant.SUCCESS);
				response.put("Data", "Y");
				
				String jsonStr = Obj.writeValueAsString(response); 
				
				//ENcryption Technique
				String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
				response1.put(Constant.STATUS, 200);                 
				response1.put(Constant.MESSAGE, Constant.SUCCESS);                
				response1.put("body", encaccessResponse); 
				return new ResponseEntity <Object>(response1,HttpStatus.OK);
			
			} 
			else 
			{
				response.put(Constant.STATUS, 201);
				response.put(Constant.MESSAGE, "Not a Super Admin User");
				response.put("Data", "N");
				
				String jsonStr = Obj.writeValueAsString(response); 
				
				//ENcryption Technique
				String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
				
				response1.put(Constant.STATUS, 201);                 
				response1.put(Constant.MESSAGE, Constant.DUPLICATE_ENTRY);                
				response1.put("body", encaccessResponse); 
				return new ResponseEntity<Object>(response1, HttpStatus.OK);
			}

		} catch (Exception e) {
			logger.error("checkUserSuperAdminOrNot exception occured." + e.getMessage());
			throw new MphBankServiceException("Internal Server Error");
		}
	}
	
	@PostMapping(value = "/usermgmt/getAllActiveInactiveAdminOrdinaryUsers")
	public ResponseEntity<Object> getAllActiveInactiveAdminOrdinaryUsers(@RequestHeader("Authorization") String token, @RequestBody EncryptionModel encryptionModel) throws MphBankServiceException 
	{
		logger.info("MphBankController : getAllActiveInactiveAdminOrdinaryUsers : initiated");

		Map<String, Object> response = new HashMap<String, Object>(4);
		response.put(Constant.STATUS, 0);
		response.put(Constant.MESSAGE, Constant.SOMETHING_WENT_WRONG);
		
		Map<String, Object> response1 = new HashMap<String, Object>();
		logger.info("encryptionModel::"+encryptionModel.getEncryptedPayload());
		ObjectMapper Obj = new ObjectMapper(); 
		try {
			JSONObject plainJSONObject = EncryptandDecryptAES.DecryptAESECBPKCS5Padding(encryptionModel.getEncryptedPayload());

			List<PortalMasterEntity> portalMasterEntities = mphBankService.getAllActiveInactiveAdminOrdinaryUsers(plainJSONObject.getString("isMphAdmin"), plainJSONObject.getString("isActive"));
			if(portalMasterEntities != null && portalMasterEntities.size() > 0) 
			{
				response.put(Constant.STATUS, 200);
				response.put(Constant.MESSAGE, Constant.SUCCESS);
				response.put("Data", portalMasterEntities);
				
				String jsonStr = Obj.writeValueAsString(response); 
				
				//ENcryption Technique
				String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
				response1.put(Constant.STATUS, 200);                 
				response1.put(Constant.MESSAGE, Constant.SUCCESS);                
				response1.put("body", encaccessResponse); 
				return new ResponseEntity <Object>(response1,HttpStatus.OK);
			
			} 
			else 
			{
				response.put(Constant.STATUS, 201);
				response.put(Constant.MESSAGE, Constant.NO_DATA_FOUND);
				
				String jsonStr = Obj.writeValueAsString(response); 
				
				//ENcryption Technique
				String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
				
				response1.put(Constant.STATUS, 201);                 
				response1.put(Constant.MESSAGE, Constant.NO_DATA_FOUND);                
				response1.put("body", encaccessResponse); 
				return new ResponseEntity<Object>(response1, HttpStatus.OK);
			}

		} catch (Exception e) {
			logger.error("getAllActiveInactiveAdminOrdinaryUsers exception occured." + e.getMessage());
			throw new MphBankServiceException("Internal Server Error");
		}
	}
	
	@PostMapping(value = "/usermgmt/getUserDetails")
	public ResponseEntity<Object> getUserDetails(@RequestHeader("Authorization") String token, @RequestBody EncryptionModel encryptionModel) throws MphBankServiceException 
	{
		logger.info("MphBankController : getUserDetails : initiated");

		Map<String, Object> response = new HashMap<String, Object>(4);
		response.put(Constant.STATUS, 0);
		response.put(Constant.MESSAGE, Constant.SOMETHING_WENT_WRONG);
		
		Map<String, Object> response1 = new HashMap<String, Object>();
		logger.info("encryptionModel::"+encryptionModel.getEncryptedPayload());
		ObjectMapper Obj = new ObjectMapper(); 
		try {
			JSONObject plainJSONObject = EncryptandDecryptAES.DecryptAESECBPKCS5Padding(encryptionModel.getEncryptedPayload());

			PortalMasterEntity portalMasterEntity = mphBankService.getUserDetails(plainJSONObject.getString("username"));
			if(portalMasterEntity != null) 
			{
				response.put(Constant.STATUS, 200);
				response.put(Constant.MESSAGE, Constant.SUCCESS);
				response.put("Data", Obj.writeValueAsString(portalMasterEntity));
				
				String jsonStr = Obj.writeValueAsString(response); 
				
				//ENcryption Technique
				String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
				response1.put(Constant.STATUS, 200);                 
				response1.put(Constant.MESSAGE, Constant.SUCCESS);                
				response1.put("body", encaccessResponse); 
				return new ResponseEntity <Object>(response1,HttpStatus.OK);
			
			} 
			else 
			{
				response.put(Constant.STATUS, 201);
				response.put(Constant.MESSAGE, Constant.NO_DATA_FOUND);
				
				String jsonStr = Obj.writeValueAsString(response); 
				
				//ENcryption Technique
				String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
				
				response1.put(Constant.STATUS, 201);                 
				response1.put(Constant.MESSAGE, Constant.NO_DATA_FOUND);                
				response1.put("body", encaccessResponse); 
				return new ResponseEntity<Object>(response1, HttpStatus.OK);
			}

		} catch (Exception e) {
			logger.error("getUserDetails exception occured." + e.getMessage());
			throw new MphBankServiceException("Internal Server Error");
		}
	}
	
	@PostMapping(value = "/usermgmt/getUnAssignedPortalsUsingMPHName")
	public ResponseEntity<Object> getUnAssignedPortalsUsingMPHName(@RequestHeader("Authorization") String token, @RequestBody EncryptionModel encryptionModel) throws MphBankServiceException 
	{
		logger.info("MphBankController : getUnAssignedPortalsUsingMPHName : initiated");

		Map<String, Object> response = new HashMap<String, Object>(4);
		response.put(Constant.STATUS, 0);
		response.put(Constant.MESSAGE, Constant.SOMETHING_WENT_WRONG);
		
		Map<String, Object> response1 = new HashMap<String, Object>();
		logger.info("encryptionModel::"+encryptionModel.getEncryptedPayload());
		ObjectMapper Obj = new ObjectMapper(); 
		try {
			JSONObject plainJSONObject = EncryptandDecryptAES.DecryptAESECBPKCS5Padding(encryptionModel.getEncryptedPayload());

			List<MasterRolesBulkEntity> masterRolesBulkEntities = mphBankService.getUnAssignedPortalsUsingMPHName(plainJSONObject.getString("mphName"));
			if(masterRolesBulkEntities != null) 
			{
				response.put(Constant.STATUS, 200);
				response.put(Constant.MESSAGE, Constant.SUCCESS);
				response.put("Data", masterRolesBulkEntities);
				
				String jsonStr = Obj.writeValueAsString(response); 
				
				//ENcryption Technique
				String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
				response1.put(Constant.STATUS, 200);                 
				response1.put(Constant.MESSAGE, Constant.SUCCESS);                
				response1.put("body", encaccessResponse); 
				return new ResponseEntity <Object>(response1,HttpStatus.OK);
			
			} 
			else 
			{
				response.put(Constant.STATUS, 201);
				response.put(Constant.MESSAGE, Constant.NO_DATA_FOUND);
				
				String jsonStr = Obj.writeValueAsString(response); 
				
				//ENcryption Technique
				String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
				
				response1.put(Constant.STATUS, 201);                 
				response1.put(Constant.MESSAGE, Constant.NO_DATA_FOUND);                
				response1.put("body", encaccessResponse); 
				return new ResponseEntity<Object>(response1, HttpStatus.OK);
			}

		} catch (Exception e) {
			logger.error("getUnAssignedPortalsUsingMPHName exception occured." + e.getMessage());
			throw new MphBankServiceException("Internal Server Error");
		}
	}
	
	@PostMapping(value = "/usermgmt/getAssignedPortalsUsingMPHName")
	public ResponseEntity<Object> getAssignedPortalsUsingMPHName(@RequestHeader("Authorization") String token, @RequestBody EncryptionModel encryptionModel) throws MphBankServiceException 
	{
		logger.info("MphBankController : getAssignedPortalsUsingMPHName : initiated");

		Map<String, Object> response = new HashMap<String, Object>(4);
		response.put(Constant.STATUS, 0);
		response.put(Constant.MESSAGE, Constant.SOMETHING_WENT_WRONG);
		
		Map<String, Object> response1 = new HashMap<String, Object>();
		logger.info("encryptionModel::"+encryptionModel.getEncryptedPayload());
		ObjectMapper Obj = new ObjectMapper(); 
		try {
			JSONObject plainJSONObject = EncryptandDecryptAES.DecryptAESECBPKCS5Padding(encryptionModel.getEncryptedPayload());

			List<MasterRolesBulkEntity> masterRolesBulkEntities = mphBankService.getAssignedPortalsUsingMPHName(plainJSONObject.getString("mphName"));
			if(masterRolesBulkEntities != null) 
			{
				response.put(Constant.STATUS, 200);
				response.put(Constant.MESSAGE, Constant.SUCCESS);
				response.put("Data", masterRolesBulkEntities);
				
				String jsonStr = Obj.writeValueAsString(response); 
				
				//ENcryption Technique
				String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
				response1.put(Constant.STATUS, 200);                 
				response1.put(Constant.MESSAGE, Constant.SUCCESS);                
				response1.put("body", encaccessResponse); 
				return new ResponseEntity <Object>(response1,HttpStatus.OK);
			
			} 
			else 
			{
				response.put(Constant.STATUS, 201);
				response.put(Constant.MESSAGE, Constant.NO_DATA_FOUND);
				
				String jsonStr = Obj.writeValueAsString(response); 
				
				//ENcryption Technique
				String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
				
				response1.put(Constant.STATUS, 201);                 
				response1.put(Constant.MESSAGE, Constant.NO_DATA_FOUND);                
				response1.put("body", encaccessResponse); 
				return new ResponseEntity<Object>(response1, HttpStatus.OK);
			}

		} catch (Exception e) {
			logger.error("getAssignedPortalsUsingMPHName exception occured." + e.getMessage());
			throw new MphBankServiceException("Internal Server Error");
		}
	}
	
	@PostMapping(value = "/usermgmt/deleteAssignedPortalsUsingMPHName")
	public ResponseEntity<Object> deleteAssignedPortalsUsingMPHName(@RequestHeader("Authorization") String token, @RequestBody EncryptionModel encryptionModel) throws MphBankServiceException 
	{
		logger.info("MphBankController : deleteAssignedPortalsUsingMPHName : initiated");

		Map<String, Object> response = new HashMap<String, Object>(4);
		response.put(Constant.STATUS, 0);
		response.put(Constant.MESSAGE, Constant.SOMETHING_WENT_WRONG);
		
		Map<String, Object> response1 = new HashMap<String, Object>();
		logger.info("encryptionModel::"+encryptionModel.getEncryptedPayload());
		ObjectMapper Obj = new ObjectMapper(); 
		try {
			JSONObject plainJSONObject = EncryptandDecryptAES.DecryptAESECBPKCS5Padding(encryptionModel.getEncryptedPayload());

			boolean portalDeleted = mphBankService.deleteAssignedPortalsUsingMPHName(plainJSONObject.getString("mphName"), plainJSONObject.getString("roleKey"));
			if(portalDeleted) 
			{
				response.put(Constant.STATUS, 200);
				response.put(Constant.MESSAGE, Constant.SUCCESS);
				response.put("Data", "Portal deleted successfully");
				
				String jsonStr = Obj.writeValueAsString(response); 
				
				//ENcryption Technique
				String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
				response1.put(Constant.STATUS, 200);                 
				response1.put(Constant.MESSAGE, Constant.SUCCESS);                
				response1.put("body", encaccessResponse); 
				return new ResponseEntity <Object>(response1,HttpStatus.OK);
			
			} 
			else 
			{
				response.put(Constant.STATUS, 201);
				response.put(Constant.MESSAGE, Constant.NO_DATA_FOUND);
				
				String jsonStr = Obj.writeValueAsString(response); 
				
				//ENcryption Technique
				String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
				
				response1.put(Constant.STATUS, 201);                 
				response1.put(Constant.MESSAGE, Constant.NO_DATA_FOUND);                
				response1.put("body", encaccessResponse); 
				return new ResponseEntity<Object>(response1, HttpStatus.OK);
			}

		} catch (Exception e) {
			logger.error("deleteAssignedPortalsUsingMPHName exception occured." + e.getMessage());
			throw new MphBankServiceException("Internal Server Error");
		}
	}
	
	@PostMapping(value = "/usermgmt/getMphNameBasedOnPolicyNumber")
	public ResponseEntity<Object> getMphNameBasedOnPolicyNumber(@RequestHeader("Authorization") String token, @RequestBody EncryptionModel encryptionModel) throws MphBankServiceException 
	{
		logger.info("MphBankController : searchBank : initiated");

		Map<String, Object> response = new HashMap<String, Object>(4);
		response.put(Constant.STATUS, 0);
		response.put(Constant.MESSAGE, Constant.SOMETHING_WENT_WRONG);
		
		Map<String, Object> response1 = new HashMap<String, Object>();
		logger.info("encryptionModel::"+encryptionModel.getEncryptedPayload());
		ObjectMapper Obj = new ObjectMapper(); 
		try {
			JSONObject plainJSONObject = EncryptandDecryptAES.DecryptAESECBPKCS5Padding(encryptionModel.getEncryptedPayload());

			MphNameEntity mphNameEntities = mphBankService.getMphnameBasedOnPolicyNumber(plainJSONObject.getString("policyNumber"));
			if(mphNameEntities == null ) 
			{
				response.put(Constant.STATUS, 201);
				response.put(Constant.MESSAGE, Constant.NO_DATA_FOUND);
				response.put(Constant.DATA, "Mph name not found with policy number");
				String jsonStr = Obj.writeValueAsString(response); 
				
				//ENcryption Technique
				String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
				response1.put(Constant.STATUS, 201);                 
				response1.put(Constant.MESSAGE, Constant.NO_DATA_FOUND);                
				response1.put("body", encaccessResponse); 
				return new ResponseEntity <Object>(response1,HttpStatus.UNAUTHORIZED);
			
			} 
			else 
			{
				response.put(Constant.STATUS, 200);
				response.put(Constant.MESSAGE, Constant.SUCCESS);
				response.put("Data", mphNameEntities);
				
				String jsonStr = Obj.writeValueAsString(response); 
				
				//ENcryption Technique
				String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
				
				response1.put(Constant.STATUS, 200);                 
				response1.put(Constant.MESSAGE, Constant.SUCCESS);                
				response1.put("body", encaccessResponse); 
				return new ResponseEntity<Object>(response1, HttpStatus.OK);
			}

		} catch (Exception e) {
			logger.error("searchBank exception occured." + e.getMessage());
			throw new MphBankServiceException("Internal Server Error");
		}
	}
	
	@PostMapping(value = "/usermgmt/assignPortalsUsingMPHName")
	public ResponseEntity<Object> assignPortalsUsingMPHName(@RequestHeader("Authorization") String token, @RequestBody EncryptionModel encryptionModel) throws MphBankServiceException 
	{
		logger.info("MphBankController : assignPortalsUsingMPHName : initiated");

		Map<String, Object> response = new HashMap<String, Object>(4);
		response.put(Constant.STATUS, 0);
		response.put(Constant.MESSAGE, Constant.SOMETHING_WENT_WRONG);
		
		Map<String, Object> response1 = new HashMap<String, Object>();
		logger.info("encryptionModel::"+encryptionModel.getEncryptedPayload());
		ObjectMapper Obj = new ObjectMapper(); 
		try {
			JSONObject plainJSONObject = EncryptandDecryptAES.DecryptAESECBPKCS5Padding(encryptionModel.getEncryptedPayload());

			boolean portalAdded = mphBankService.assignPortalsUsingMPHName(plainJSONObject.getString("mphName"), plainJSONObject.getString("roleKey"));
			if(portalAdded) 
			{
				response.put(Constant.STATUS, 200);
				response.put(Constant.MESSAGE, Constant.SUCCESS);
				response.put("Data", "Portal assigned successfully");
				
				String jsonStr = Obj.writeValueAsString(response); 
				
				//ENcryption Technique
				String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
				response1.put(Constant.STATUS, 200);                 
				response1.put(Constant.MESSAGE, Constant.SUCCESS);                
				response1.put("body", encaccessResponse); 
				return new ResponseEntity <Object>(response1,HttpStatus.OK);
			
			} 
			else 
			{
				response.put(Constant.STATUS, 201);
				response.put(Constant.MESSAGE, Constant.NO_DATA_FOUND);
				
				String jsonStr = Obj.writeValueAsString(response); 
				
				//ENcryption Technique
				String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
				
				response1.put(Constant.STATUS, 201);                 
				response1.put(Constant.MESSAGE, Constant.NO_DATA_FOUND);                
				response1.put("body", encaccessResponse); 
				return new ResponseEntity<Object>(response1, HttpStatus.OK);
			}

		} catch (Exception e) {
			logger.error("assignPortalsUsingMPHName exception occured." + e.getMessage());
			throw new MphBankServiceException("Internal Server Error");
		}
	}
}