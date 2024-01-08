package com.lic.epgs.mst.usermgmt.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.codehaus.jettison.json.JSONArray;
import org.json.HTTP;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lic.epgs.constant.Constant;
import com.lic.epgs.mst.usermgmt.encryptdecrypt.EncryptandDecryptAES;
import com.lic.epgs.mst.usermgmt.entity.MPHSuperAdminUsersEntity;
import com.lic.epgs.mst.usermgmt.entity.MasterPolicyDataEntity;
import com.lic.epgs.mst.usermgmt.entity.MasterRolesBulkEntity;
import com.lic.epgs.mst.usermgmt.entity.MasterUsersEntity;
import com.lic.epgs.mst.usermgmt.entity.MphNameEntity;
import com.lic.epgs.mst.usermgmt.entity.PortalMasterEntity;
import com.lic.epgs.mst.usermgmt.exceptionhandling.MphBankServiceException;
import com.lic.epgs.mst.usermgmt.repository.MPHSuperAdminUsersRepository;
import com.lic.epgs.mst.usermgmt.repository.MasterPolicyDataRepository;
import com.lic.epgs.mst.usermgmt.repository.MasterRolesBulkRepository;
import com.lic.epgs.mst.usermgmt.repository.MasterUsersRepository;
import com.lic.epgs.mst.usermgmt.repository.MphNameRepository;
import com.lic.epgs.mst.usermgmt.repository.PortalMasterRepository;
import com.lic.epgs.rhssomodel.AddUserListModel;
import com.lic.epgs.rhssomodel.AddUserModel;
import com.lic.epgs.rhssomodel.Credential;
import com.lic.epgs.rhssomodel.ResponseModel;
import com.lic.epgs.rhssomodel.UserGroupResponse;
import com.lic.epgs.rhssomodel.UserResponse;
import com.lic.epgs.rhssomodel.UserResponseModel;

@Service
@Transactional
@CacheConfig(cacheNames = "masterCache")
public class MphBankServiceImpl implements MphBankService{

	private static final Logger logger = LoggerFactory.getLogger(MphBankServiceImpl.class);
	
	@Autowired
	private MphNameRepository mphNameRepository;
	
	@Autowired
	private EncryptandDecryptAES encryptandDecryptAES;
	
	@Autowired
    MasterPolicyDataRepository masterPolicyDataRepository;
	
	RestTemplate restTemplate = new RestTemplate();
	
	@Autowired
	private PortalMasterRepository portalMasterRepository;
	
	@Value("${application.url}")
	private String applicationurl;
	
	@Value("${rhsso.url}")
	private String rhssoUrl; 
	
	@Autowired
	private RedhatUserGenerationService redhatUserGenerationService;
	
	@Autowired
	private MphUserService mphUserService;
	
	@Autowired
	private MPHSuperAdminUsersRepository mphSuperAdminUsersRepository;
	
	@Autowired
	MasterRolesBulkRepository masterRolesBulkRepository;
	
	@Override
	public List<MphNameEntity> searchBankDetails(String bankName) throws Exception 
	{
		logger.info("searchBankDetails method started");
		try
		{
			return mphNameRepository.findMphDetailsUsingMphName(bankName);
		}
		catch(Exception e)
		{
			logger.debug("Exception Occurred while fetching MPH Details ::"+e.getMessage());
		}
		logger.info("searchBankDetails method ended");
		return null;
	}

	@Override
	public JSONObject getAllBankDetails(String bankName) throws Exception 
	{
		logger.info("getAllBankDetails method started");
		JSONObject responseObject = null;
		try
		{
			 JSONObject jsonObj = new JSONObject();
			 jsonObj.put("mphName", bankName);
			 HttpHeaders headers = new HttpHeaders();
			 headers.setContentType(MediaType.APPLICATION_JSON);
			 headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
			 HttpEntity formEntity = new HttpEntity(jsonObj.toString(),headers);
			 
			 ResponseEntity responseFromApi = restTemplate.exchange(applicationurl+"/pmjjbyservice/LIC_ePGS/ChallanManagement/getPolicyNumberListFilter", HttpMethod.POST, formEntity,String.class); 
			 JSONArray policyNumberArray = new JSONArray(responseFromApi.getBody().toString());
			 String policyNumber = "";
			 for(int i = 0; i < policyNumberArray.length(); i++)
			 {
				 if(policyNumberArray.getJSONObject(i).has("mphName") && policyNumberArray.getJSONObject(i).getString("mphName").equalsIgnoreCase(bankName))
				 {
					 if(policyNumberArray.getJSONObject(i).has("policyNumber"))
					 {
						 policyNumber = policyNumberArray.getJSONObject(i).getString("policyNumber");
						 break;
					 }
				 }
			 }
			 long mphUsers = portalMasterRepository.getAllMPHOrdinaryUsersUnderThatMphName(bankName);
			 long mphAdminUsers = portalMasterRepository.getAllMPHAdminUsersUnderThatMphName(bankName);
			 responseObject = new JSONObject();
			 responseObject.put("policyNumber", policyNumber);
			 responseObject.put("bankName", bankName);
			 responseObject.put("totalNoOfMPHOrdinaryUsers", mphUsers);
			 responseObject.put("totalNoOfMPHAdminUsers", mphAdminUsers);
		}
		catch(Exception e)
		{
			logger.debug("Exception Occurred while fetching All MPH Details ::"+e.getMessage());
		}
		logger.info("getAllBankDetails method ended");
		return responseObject;
	}

	@Override
	public List<MphNameEntity> getAllBankNames(String isActive) throws Exception 
	{
		logger.info("getAllBankNames method started");
		try 
		{
			if(isActive.equalsIgnoreCase("Y"))
			{
				return mphNameRepository.findActiveInActiveMphDetails(isActive, "N");
			}
			else
			{
				return mphNameRepository.findActiveInActiveMphDetails(isActive, "Y");
			}
		}
		catch(Exception e)
		{
			logger.debug("Exception Occurred while fetching All MPH Details ::"+e.getMessage());
		}
		logger.info("getAllBankNames method ended");
		return null;
	}

	@Override
	public boolean deleteAllBankDetails(String bankName, String token) throws Exception 
	{
		logger.info("deleteAllBankDetails method started");
		boolean bankDeleted = false;
		try
		{
			long count = portalMasterRepository.getAllUsersUnderThatMphName(bankName);
			AtomicInteger deleteUserCount = new AtomicInteger();
			if(count > 0 && count <= 20)
			{
				List<PortalMasterEntity> portalMasterEntities = portalMasterRepository.getAllUsersInThatMphName(bankName);
				portalMasterEntities.stream().forEach(obj -> {
					String userId = "";
					 try {
						   ResponseEntity<String> rrm = redhatUserGenerationService.searchUserByUsernameForSuperAdmin(token,Constant.RHSSO_REALM, obj.getUsername());
						   
						   if(rrm.getStatusCode().equals(HttpStatus.OK))
						   {	
							   	JSONObject plainJSONObject = new JSONObject();
								logger.debug("Useranme response from RHSSO API " + rrm.getBody());
								plainJSONObject = EncryptandDecryptAES.DecryptAESECBPKCS5Padding(rrm.getBody());
							 	logger.debug("Username JSON plain object " + plainJSONObject);
							 	UserResponseModel urm = new ObjectMapper().readValue(plainJSONObject.toString(), UserResponseModel.class);
							 	for (UserResponse userResponse : urm.getUserlist()) 
							 	{
									if(userResponse.getUsername().equalsIgnoreCase(obj.getUsername()))
									{
										userId = userResponse.getId();
										break;
									}
								}
						   }
						 } 
						 catch(Exception e) {
							 logger.debug("fetching userid from RHSSO exception occured :" + e.getMessage());
						 }
					 try 
					 {
						ResponseEntity<Object> deleteUserResponse = redhatUserGenerationService.deleteUser(token, Constant.RHSSO_REALM, userId);
						JSONObject plainJSONObject = EncryptandDecryptAES.DecryptAESECBPKCS5Padding(deleteUserResponse.getBody().toString());
	            	 	logger.debug("deleteAllBankDetails deleted user response json : " + plainJSONObject);
	            	 	ResponseModel rm = new ObjectMapper().readValue(plainJSONObject.toString(), ResponseModel.class);
	            	 	if(rm != null && rm.getStatus() == 200)
						{
	            	 		portalMasterRepository.findAndDeleteById(obj.getPortalUserId());
	            	 		deleteUserCount.getAndIncrement();
						}
					 } 
					 catch (Exception e) 
					 {
						logger.debug("deleting user from RHSSO exception occured :" + e.getMessage());
					 }
					
					});
				if(deleteUserCount.get() == count)
				{
					mphNameRepository.deleteBankNameByUpdatingItsFlag(bankName);
					bankDeleted = true;
				}
			}
		}
		catch(Exception e)
		{
			logger.debug("Exception Occurred while deleting All MPH Details ::"+e.getMessage());
		}
		logger.info("deleteAllBankDetails method ended");
		return bankDeleted;
		
	}

	@Override
	public boolean checkBankAvailability(String bankName) throws Exception 
	{
		logger.info("checkBankAvailability method started");
		boolean bankAvailable = false;
		try
		{
			MphNameEntity mphNameEntity = mphNameRepository.checkMphAvailability(bankName);
			
			if(mphNameEntity != null)
			{
				if(mphNameEntity.getFirstAdminCreated() == 'Y')
				{
					bankAvailable = true;
				}
			}
		}
		catch(Exception e)
		{
			logger.debug("Exception Occurred while checking Bank Availability ::"+e.getMessage());
		}
		logger.info("checkBankAvailability method ended");
		return bankAvailable;
	}
	
	@Override
	public JSONObject saveFirstAdminDetailsInDBAndRHSSO(JSONObject adminUserObj, String token) throws Exception 
	{
		logger.info("saveFirstAdminDetailsInDBAndRHSSO method started");
		JSONObject userAddedDetails = null;
		String groupId = "";
		String userId = "";
		try
		{
			String mphKey = "MPH_"+adminUserObj.getString("bankName").substring(0, 3)+String.valueOf(new Random().nextInt(10000)).toUpperCase();
			logger.info("MPH Key generated :: "+mphKey);
			final String baseUrl = rhssoUrl + "/auth/admin/realms/{realm}/groups";
			
			 HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.AUTHORIZATION, token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			
			
			Map<String, String> uriParam = new HashMap<>();
			uriParam.put("realm",Constant.RHSSO_REALM);
			
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("name", mphKey);
			
			
			HttpEntity formEntity = new HttpEntity(jsonObj.toString(), headers);
			logger.debug("formEntity "+ formEntity);
	    	//ObjectMapper mapper = new ObjectMapper();
			
			logger.debug("baseUrl:"+baseUrl);
			logger.debug("formEntity:"+formEntity);
			//ResponseEntity<Object> response = restTemplate.postForEntity(baseUrl, formEntity, Object.class, uriParam);
			ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, formEntity, String.class, uriParam);
			logger.debug("response for add group "+ response);
			logger.debug("response for add group 2 "+ response.getStatusCode());
			if (response.getStatusCode().equals(HttpStatus.CREATED) && response.getStatusCodeValue() == 201) 
			{
				
				String url = rhssoUrl + "/auth/admin/realms/{realms}/groups?first=0&max=20&search="+mphKey;
				UriComponents groupApibuilder = UriComponentsBuilder.fromHttpUrl(url)
						.build();
				HttpEntity<MultiValueMap<String, String>> entity1= new HttpEntity<>(headers);
				Map<String, String> uriParam1 = new HashMap<>();
				uriParam1.put("realms", Constant.RHSSO_REALM);
				ResponseEntity<UserGroupResponse[]> groupEntityResponse = restTemplate.exchange(groupApibuilder.toUriString(),HttpMethod.GET, entity1, UserGroupResponse[].class, uriParam1);
				List<UserGroupResponse> groupObj = Arrays.asList(groupEntityResponse.getBody());
				for (UserGroupResponse userGroupResponse : groupObj) 
				{
					if(userGroupResponse.getName().equalsIgnoreCase(mphKey))
					{
						groupId = userGroupResponse.getId();
					}
				}
				Date today = new Date();
				MphNameEntity mphNameEntity = new MphNameEntity();
				mphNameEntity.setMphName(adminUserObj.getString("bankName"));
				mphNameEntity.setMphCode("");
				mphNameEntity.setMphKey(mphKey);
				mphNameEntity.setIsActive('Y');
				mphNameEntity.setIsDeleted('N');
				mphNameEntity.setCreatedBy("sysadmin");
				mphNameEntity.setCreatedOn(today);
				mphNameEntity.setModifiedBy("sysadmin");
				mphNameEntity.setModifiedOn(today);
				mphNameEntity.setMphAdminMaxCOunt(5);
				mphNameEntity.setFirstAdminCreated('Y');
				mphNameEntity.setPortalsAssigned(adminUserObj.getString("roleKey"));
				long maxMphId = mphNameRepository.count();
				mphNameEntity.setMphId(String.valueOf(maxMphId+1));
				MphNameEntity entity = null;
				try
				{
					entity = mphNameRepository.save(mphNameEntity);
				}
				catch(Exception e)
				{
					logger.error("Could not add MPH in DB ::"+ e.getMessage());
					e.printStackTrace();
				}
				if(entity != null)
				{
					ArrayList<Credential> credentialList = new ArrayList();
					ArrayList<String> grouplist = new ArrayList();
					AddUserModel addUserModel = new AddUserModel();
					AddUserListModel addUserModel1 = new AddUserListModel();
					String realm = adminUserObj.getString("realm");
					addUserModel.setUsername(adminUserObj.getString("username"));
					addUserModel.setEnabled(true);
					addUserModel.setFirstName(adminUserObj.getString("fullName"));
					addUserModel.setLastName(adminUserObj.getString("fullName"));
					addUserModel.setEmail(adminUserObj.getString("email"));
					addUserModel.setGroups(mphKey);
					addUserModel.setPwdd(adminUserObj.getString("username"));
					grouplist.add(addUserModel.getGroups());
					addUserModel1.setUsername(addUserModel.getUsername());
					addUserModel1.setEnabled(true);
					addUserModel1.setFirstName(addUserModel.getFirstName());
					addUserModel1.setLastName(addUserModel.getLastName());
					addUserModel1.setEmail(addUserModel.getEmail());
					addUserModel1.setGroups(grouplist);
					Credential credentials = new Credential();
					credentials.setType("password");
					credentials.setValue(addUserModel.getPwdd());
					credentialList.add(credentials);
	
					addUserModel1.setCredentials(credentialList);
	
					ResponseEntity<Object> rm = redhatUserGenerationService.addUser(token, realm, addUserModel1);
					if(rm.getStatusCode().equals(HttpStatus.OK))
					{
						PortalMasterEntity portalMasterEntity = new PortalMasterEntity();
						
						portalMasterEntity.setUsername(adminUserObj.getString("username").toLowerCase());
						portalMasterEntity.setMobile(Long .parseLong(adminUserObj.getString("mobile")));
						portalMasterEntity.setEmail(adminUserObj.getString("email"));
						portalMasterEntity.setState("");
						portalMasterEntity.setCity("");
						portalMasterEntity.setDistrict("");
						portalMasterEntity.setCreatedBy(adminUserObj.getString("createdBy"));
						portalMasterEntity.setCreatedOn(today);
						portalMasterEntity.setModifiedBy(adminUserObj.getString("modifiedBy"));
						portalMasterEntity.setModifiedOn(today);
						portalMasterEntity.setIsActive("Y");
						portalMasterEntity.setIsDeleted("N");
						portalMasterEntity.setRefreshToken("");
						portalMasterEntity.setMphName(adminUserObj.getString("bankName"));
						portalMasterEntity.setIsMphAdmin("Y");
						portalMasterEntity.setLogOut("N");
						portalMasterEntity.setFullName(adminUserObj.getString("fullName"));
						portalMasterEntity.setAssignRoleFlag("Y");
						portalMasterEntity.setOfficeName(adminUserObj.getString("mphOfficeName"));
						portalMasterEntity.setOfficeCode(adminUserObj.getString("mphOfficeCode"));
						portalMasterEntity.setIsNewUser("Y");
						
						ResponseEntity<Object> userResponse =  mphUserService.addMphUser(portalMasterEntity);
						if(userResponse.getStatusCode().equals(HttpStatus.OK))
						{
							mphNameRepository.updatingFirstAdminFlag(adminUserObj.getString("bankName"));
							try
							{
								ResponseEntity<String> allUsersResponse = redhatUserGenerationService.searchUserByEmail(token, Constant.RHSSO_REALM, adminUserObj.getString("email"));
				            	if(allUsersResponse.getStatusCode().equals(HttpStatus.OK))
				            	{	
				            		logger.debug("saveFirstAdminDetailsInDBAndRHSSO response from RHSSO API " + allUsersResponse.getBody());
				            		JSONObject plainJSONObject = EncryptandDecryptAES.DecryptAESECBPKCS5Padding(allUsersResponse.getBody());
				            	 	logger.debug("saveFirstAdminDetailsInDBAndRHSSO JSON plain object " + plainJSONObject);
				            	 	ObjectMapper ob = new ObjectMapper();
				            	 	UserResponseModel urm = ob.readValue(plainJSONObject.toString(), UserResponseModel.class);
				            	 	for (UserResponse userResp : urm.getUserlist()) 
				            	 	{
										if(userResp.getEmail().equalsIgnoreCase(adminUserObj.getString("email")))
										{
											userId = userResp.getId();
											logger.debug("saveFirstAdminDetailsInDBAndRHSSO user id is : " + userId);	
										}
									}
				            	}
							}
							catch(Exception e)
							{
								logger.error("Unable to fetch userid from RHSSO API " + e.getMessage());
							}
							userAddedDetails = new JSONObject();
		            	 	userAddedDetails.put("userName", adminUserObj.getString("username").toLowerCase());
		            	 	userAddedDetails.put("userId", userId);
						}
					}
				}
				else
				{
					deleteGroup(groupId, token);
				}
			}
		}
		catch(Exception e)
		{
			logger.debug("Exception Occurred while adding first admin user deatils ::"+e.getMessage());
			deleteGroup(groupId, token);
			e.printStackTrace();
		}
		logger.info("saveFirstAdminDetailsInDBAndRHSSO method ended");
		return userAddedDetails;
	}

	@Override
	public boolean deleteGroup(String groupId, String token) throws Exception 
	{
		logger.info("deleteGroup method started");
		boolean groupDeleted = false;
		try
		{
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.AUTHORIZATION, token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			String deleteApiUrl = rhssoUrl + "/auth/admin/realms/{realms}/groups/{groupId}";
			Map<String, String> uriParam = new HashMap<>();
			uriParam.put("realms", Constant.RHSSO_REALM);
			uriParam.put("groupId", groupId);
			UriComponents deleteApibuilder = UriComponentsBuilder.fromHttpUrl(deleteApiUrl).build();
			HttpEntity deleteEntity = new HttpEntity<>(headers);
			ResponseEntity<?> deleteGroupResponse = restTemplate.exchange(deleteApibuilder.toUriString(), HttpMethod.DELETE,deleteEntity, String.class, uriParam);
			if (deleteGroupResponse.getStatusCode().equals(HttpStatus.NO_CONTENT) && deleteGroupResponse.getStatusCodeValue() == 204) 
			{
				groupDeleted = true;
			}
		}
		catch(Exception e)
		{
			logger.debug("Exception Occurred while deleting group from RHSSO ::"+e.getMessage());
		}
		logger.info("deleteGroup method ended");
		return groupDeleted;
	}

	@Override
	public boolean checkUserSuperAdminOrNot(String username) throws Exception 
	{
		logger.info("checkUserSuperAdminOrNot method started");
		boolean superAdminUser = false;
		try
		{
			MPHSuperAdminUsersEntity mphSuperAdminUsersEntity = mphSuperAdminUsersRepository.getSuperAdminUserDetails(username);
			
			if(mphSuperAdminUsersEntity != null)
			{
				superAdminUser = true;
			}
		}
		catch(Exception e)
		{
			logger.debug("Exception Occurred while checking Bank Availability ::"+e.getMessage());
		}
		logger.info("checkUserSuperAdminOrNot method ended");
		return superAdminUser;
	}

	@Override
	public List<PortalMasterEntity> getAllActiveInactiveAdminOrdinaryUsers(String is_MphAdmin, String is_Active) throws Exception 
	{
		logger.info("getAllActiveInactiveAdminOrdinaryUsers method started");
		List<PortalMasterEntity> activeInactiveAdminOrdinaryUsers = null;
		try
		{
			String is_deleted = "";
			if(is_Active.equalsIgnoreCase("Y"))
			{
				is_deleted = "N";
//				activeInactiveAdminOrdinaryUsers = portalMasterRepository.getAllAdminOrdinaryUsers1();
			}
			else
			{
				is_deleted = "Y";
//				activeInactiveAdminOrdinaryUsers = portalMasterRepository.getAllAdminOrdinaryUsers1();
			}
		}
		catch(Exception e)
		{
			logger.debug("Exception Occurred while fetching Active/InActive Users ::"+e.getMessage());
			e.printStackTrace();
		}
		logger.info("getAllActiveInactiveAdminOrdinaryUsers method ended");
		return activeInactiveAdminOrdinaryUsers;
	}

	@Override
	public PortalMasterEntity getUserDetails(String username) throws Exception 
	{
		logger.info("getUserDetails method started");
		try
		{
			return portalMasterRepository.getMasterUserIdByUserName(username);
		}
		catch(Exception e)
		{
			logger.debug("Exception Occurred while fetching Users ::"+e.getMessage());
		}
		logger.info("getUserDetails method ended");
		return null;
	}
	
	@Override
	public List<MasterRolesBulkEntity> getUnAssignedPortalsUsingMPHName(String mphName) throws Exception 
	{
		logger.info("getUnAssignedPortalsUsingMPHName method started");
		List<MasterRolesBulkEntity> unAssignedRolesList = null;
		try
		{
			unAssignedRolesList = new ArrayList<MasterRolesBulkEntity>();
			MphNameEntity mphNameEntity = mphNameRepository.checkMphAvailability(mphName);
			List<MasterRolesBulkEntity> allRolesList = masterRolesBulkRepository.getAllModulesBulkWithoutRoleType();
			if(mphNameEntity.getPortalsAssigned() != null && !mphNameEntity.getPortalsAssigned().isEmpty())
			{
				List<String> portalsAssignedList = new ArrayList<>(); 
				 
				if(mphNameEntity.getPortalsAssigned().contains(","))
				{
					Collections.addAll(portalsAssignedList, mphNameEntity.getPortalsAssigned().split(",")); 
				}
				else
				{
					portalsAssignedList.add(mphNameEntity.getPortalsAssigned());
				}
				 
				unAssignedRolesList = allRolesList.stream().filter((masterRolesBulkEntity) -> !portalsAssignedList.contains(masterRolesBulkEntity.getRoleKey())).collect(Collectors.toList());
			}
			else
			{
				unAssignedRolesList = allRolesList.stream().collect(Collectors.toList());
			}
		}
		catch(Exception e)
		{
			logger.debug("Exception Occurred while fetching UnAssigned Portals ::"+e.getMessage());
		}
		logger.info("getUnAssignedPortalsUsingMPHName method ended");
		return unAssignedRolesList;
	}
	
	@Override
	public List<MasterRolesBulkEntity> getAssignedPortalsUsingMPHName(String mphName) throws Exception 
	{
		logger.info("getAssignedPortalsUsingMPHName method started");
		List<MasterRolesBulkEntity> assignedRolesList = null;
		try
		{
			assignedRolesList = new ArrayList<MasterRolesBulkEntity>();
			MphNameEntity mphNameEntity = mphNameRepository.checkMphAvailability(mphName);
			List<MasterRolesBulkEntity> allRolesList = masterRolesBulkRepository.getAllModulesBulkWithoutRoleType();
			if(mphNameEntity.getPortalsAssigned() != null && !mphNameEntity.getPortalsAssigned().isEmpty())
			{
				List<String> portalsAssignedList = new ArrayList<>(); 
				 
				if(mphNameEntity.getPortalsAssigned().contains(","))
				{
					Collections.addAll(portalsAssignedList, mphNameEntity.getPortalsAssigned().split(",")); 
				}
				else
				{
					portalsAssignedList.add(mphNameEntity.getPortalsAssigned());
				}
				 
				assignedRolesList = allRolesList.stream().filter((masterRolesBulkEntity) -> portalsAssignedList.contains(masterRolesBulkEntity.getRoleKey())).collect(Collectors.toList());
			}
			else
			{
				assignedRolesList = allRolesList.stream().collect(Collectors.toList());
			}
		}
		catch(Exception e)
		{
			logger.debug("Exception Occurred while fetching Assigned Portals ::"+e.getMessage());
		}
		logger.info("getAssignedPortalsUsingMPHName method ended");
		return assignedRolesList;
	}
	
	@Override
	public boolean deleteAssignedPortalsUsingMPHName(String mphName, String roleKey) throws Exception 
	{
		logger.info("deleteAssignedPortalsUsingMPHName method started");
		boolean portalDeleted = false;
		try
		{
			MphNameEntity mphNameEntity = mphNameRepository.checkMphAvailability(mphName);
			if(mphNameEntity.getPortalsAssigned() != null && !mphNameEntity.getPortalsAssigned().isEmpty())
			{
				List<String> portalsAssignedList = new ArrayList<>();
				int updateResponse = 0;
				 
				if(mphNameEntity.getPortalsAssigned().contains(","))
				{
					Collections.addAll(portalsAssignedList, mphNameEntity.getPortalsAssigned().split(","));
					List<String> assignedRolesList = portalsAssignedList.stream().filter((s) -> !s.equalsIgnoreCase(roleKey)).collect(Collectors.toList());
					
					updateResponse = mphNameRepository.deletePortalsByMphName(roleKey, mphName, String.join(",", assignedRolesList));
				}
				else
				{
					updateResponse = mphNameRepository.deletePortalsByMphName(roleKey, mphName, "");
				}
				
				if(updateResponse == 1)
				{
					portalDeleted = true;
				}
			}
		}
		catch(Exception e)
		{
			logger.debug("Exception Occurred while deleting Assigned Portals ::"+e.getMessage());
		}
		logger.info("deleteAssignedPortalsUsingMPHName method ended");
		return portalDeleted;
	}
	
	@Override
	public boolean assignPortalsUsingMPHName(String mphName, String roleKey) throws Exception 
	{
		logger.info("assignPortalsUsingMPHName method started");
		boolean portalAdded = false;
		try
		{
			int updateResponse = 0;
			MphNameEntity mphNameEntity = mphNameRepository.checkMphAvailability(mphName);
			if(mphNameEntity.getPortalsAssigned() != null && !mphNameEntity.getPortalsAssigned().isEmpty())
			{
				List<String> portalsAssignedList = new ArrayList<>();
				if(mphNameEntity.getPortalsAssigned().contains(","))
				{
					Collections.addAll(portalsAssignedList, mphNameEntity.getPortalsAssigned().split(","));
					if(!portalsAssignedList.contains(roleKey))
					{
						portalsAssignedList.add(roleKey);
					}
					updateResponse = mphNameRepository.updatePortalsByMphName(mphName, String.join(",", portalsAssignedList));
				}
				else
				{
					portalsAssignedList.add(mphNameEntity.getPortalsAssigned());
					portalsAssignedList.add(roleKey);
					updateResponse = mphNameRepository.updatePortalsByMphName(mphName, String.join(",", portalsAssignedList));
				}
			}
			else
			{
				updateResponse = mphNameRepository.updatePortalsByMphName(mphName, roleKey);
			}
			if(updateResponse == 1)
			{
				portalAdded = true;
			}
		}
		catch(Exception e)
		{
			logger.debug("Exception Occurred while Assigning Portals ::"+e.getMessage());
		}
		logger.info("assignPortalsUsingMPHName method ended");
		return portalAdded;
	}
	
//	@Override
//	public ResponseEntity<Object> checkPolicyAvailability(String policyNumber, String loggedInUserUnitCode) throws Exception 
//	{
//		 
//		logger.info("checkPolicyAvailability method started");
//		Map<String, Object> response = new HashMap<String, Object>(4);
//		response.put(Constant.STATUS, 0);
//		response.put(Constant.MESSAGE, Constant.SOMETHING_WENT_WRONG);
//		ObjectMapper Obj = new ObjectMapper(); 
//		Map<String, Object> response1 = new HashMap<String, Object>();
//		boolean policynumber = false;
//		try
//		{
//			MasterPolicyDataEntity policyData = masterPolicyDataRepository.checkPolicyAvailability(policyNumber);
//			if(loggedInUserUnitCode != null && policyData != null && policyData.getUnitCode() != null && !loggedInUserUnitCode.equalsIgnoreCase(policyData.getUnitCode())) {
//				response.put(Constant.STATUS, 201);
//				response.put(Constant.MESSAGE, Constant.ERROR);
//				response.put(Constant.DATA, "This policy belongs to different unit");				
//				String jsonStr = Obj.writeValueAsString(response); 			
//				String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
//				response1.put(Constant.STATUS, 201);                 
//				response1.put(Constant.MESSAGE, Constant.ERROR);                
//				response1.put("body", encaccessResponse); 
//				return new ResponseEntity <Object>(response1,HttpStatus.OK);
//			}
//			
//			if(policyData!=null) {
//				
//				MphNameEntity mphNameEntity = mphNameRepository.checkMphAvailabilityBasedOnPolicyNumber(policyNumber);
//				
//				if (mphNameEntity== null) {
//					        
//					 String firstLetterWord = policyData.getMphName();
//					    String str = "";
//					        boolean v = true;
//					        for (int i = 0; i < firstLetterWord.length(); i++)
//					        {
//					            // If it is space, set v as true.
//					            if (firstLetterWord.charAt(i) == ' ')
//					            {
//					                v = true;
//					            }
//					            else if (firstLetterWord.charAt(i) != ' ' && v == true)
//					            {
//					            	
//					                str += (firstLetterWord.charAt(i));
//					                v = false;
//					            }
//					        }
//					 
//					Date date = new Date();
//					MphNameEntity mphEntity = new MphNameEntity();
//					mphEntity.setMphName(policyData.getMphName());
//					mphEntity.setMphCode(policyData.getMphCode());
//					mphEntity.setMphKey("MPH_"+str);
//				    mphEntity.setIsActive('Y');
//				    mphEntity.setIsDeleted('N');
//				    mphEntity.setCreatedBy("sysadmin");
//				    mphEntity.setCreatedOn(date);
//				    mphEntity.setModifiedBy("sysadmin");
//				    mphEntity.setModifiedOn(date);
//				    mphEntity.setMphAdminMaxCOunt(25L);
//				    mphEntity.setFirstAdminCreated('N');
//				    mphEntity.setPortalsAssigned(null);
//				    mphEntity.setPolicyNumber(policyData.getPolicyNumber());
//				    long maxMphId = mphNameRepository.count();
//					mphEntity.setMphId(String.valueOf(maxMphId+1));
//					MphNameEntity entity = null;
//			
//					entity =  mphNameRepository.save(mphEntity);
//					if(entity != null)
//					{
//						final String baseUrl = rhssoUrl + "/auth/admin/realms/{realm}/groups";
//						
//						 HttpHeaders headers = new HttpHeaders();
//						headers.add(HttpHeaders.AUTHORIZATION,"Bearer "+mphUserService.generateToken().getAccess_token());
//						headers.setContentType(MediaType.APPLICATION_JSON);
//						
//						
//						Map<String, String> uriParam = new HashMap<>();
//						uriParam.put("realm",Constant.RHSSO_REALM);
//						
//						JSONObject jsonObj = new JSONObject();
//						jsonObj.put("name", "MPH_"+str);
//						
//						
//						HttpEntity formEntity = new HttpEntity(jsonObj.toString(), headers);
//						logger.debug("formEntity "+ formEntity);
//				    	//ObjectMapper mapper = new ObjectMapper();
//						
//						logger.debug("baseUrl:"+baseUrl);
//						logger.debug("formEntity:"+formEntity);
//						//ResponseEntity<Object> response = restTemplate.postForEntity(baseUrl, formEntity, Object.class, uriParam);
//						ResponseEntity<String> res = restTemplate.postForEntity(baseUrl, formEntity, String.class, uriParam);
//						logger.debug("response for add group "+ res);
//						logger.debug("response for add group 2 "+ res.getStatusCode());
//						
//						MphNameEntity mphName = mphNameRepository.checkMphAvailabilityBasedOnPolicyNumber(policyNumber);
//						
//						if(mphName.getFirstAdminCreated() == 'Y')
//						{
//							policynumber = true;
//							response.put(Constant.STATUS, 201);
//							response.put(Constant.MESSAGE, Constant.ERROR);
//							response.put(Constant.DATA, "First Admin already present");
//							
//							String jsonStr = Obj.writeValueAsString(response); 
//							
//							//ENcryption Technique
//							String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
//							response1.put(Constant.STATUS, 201);                 
//							response1.put(Constant.MESSAGE, Constant.ERROR);                
//							response1.put("body", encaccessResponse); 
//							return new ResponseEntity <Object>(response1,HttpStatus.OK);
//							
//						}else {
//						response.put(Constant.STATUS, 200);
//						response.put(Constant.MESSAGE, Constant.SUCCESS);
//						response.put(Constant.DATA, mphName.getMphName());
//						String jsonStr = Obj.writeValueAsString(response); 
//						
//						//ENcryption Technique
//						String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
//						response1.put(Constant.STATUS, 200);                 
//						response1.put(Constant.MESSAGE, Constant.SUCCESS);                
//						response1.put("body", encaccessResponse); 
//						return new ResponseEntity <Object>(response1,HttpStatus.OK);
//					}
//					
//				}
//				        }
//			
//				
//			
//				else{
//					
//					if(mphNameEntity.getFirstAdminCreated() == 'Y')
//					{
//						policynumber = true;
//						response.put(Constant.STATUS, 201);
//						response.put(Constant.MESSAGE, Constant.ERROR);
//						response.put(Constant.DATA, "First Admin already present");
//						
//						String jsonStr = Obj.writeValueAsString(response); 
//						
//						//ENcryption Technique
//						String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
//						response1.put(Constant.STATUS, 201);                 
//						response1.put(Constant.MESSAGE, Constant.ERROR);                
//						response1.put("body", encaccessResponse); 
//						return new ResponseEntity <Object>(response1,HttpStatus.OK);
//						
//					}else {
//					response.put(Constant.STATUS, 200);
//					response.put(Constant.MESSAGE, Constant.SUCCESS);
//					response.put(Constant.DATA, mphNameEntity.getMphName());
//					String jsonStr = Obj.writeValueAsString(response); 
//					
//					//ENcryption Technique
//					String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
//					response1.put(Constant.STATUS, 200);                 
//					response1.put(Constant.MESSAGE, Constant.SUCCESS);                
//					response1.put("body", encaccessResponse); 
//					return new ResponseEntity <Object>(response1,HttpStatus.OK);
//				}
//			}
//				
//			}
//			else {
//				response.put(Constant.STATUS, 201);
//				response.put(Constant.MESSAGE, Constant.ERROR);
//				response.put(Constant.DATA, "Policy Details not present");
//				
//				String jsonStr = Obj.writeValueAsString(response); 
//				
//				//ENcryption Technique
//				String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
//				response1.put(Constant.STATUS, 201);                 
//				response1.put(Constant.MESSAGE, Constant.ERROR);                
//				response1.put("body", encaccessResponse); 
//				return new ResponseEntity <Object>(response1,HttpStatus.OK);
//				
//			}
//			
//			        
//			
//		
//		}
//			
//		catch(Exception e)
//		{
//			logger.debug("Exception Occurred while checking Policy Availability ::"+e.getMessage());
//				response.put(Constant.STATUS, 201);
//				response.put(Constant.MESSAGE, Constant.ERROR);
//				response.put(Constant.DATA, "Policy Details not present");
//				
//				String jsonStr = Obj.writeValueAsString(response); 
//				
//				//ENcryption Technique
//				String encaccessResponse = encryptandDecryptAES.EncryptAESECBPKCS5Padding(jsonStr);
//				response1.put(Constant.STATUS, 201);                 
//				response1.put(Constant.MESSAGE, Constant.ERROR);                
//				response1.put("body", encaccessResponse); 
//		}
//		logger.info("checkPolicyAvailability method ended");
//		return new ResponseEntity <Object>(response1,HttpStatus.OK);
//	}

	@Override
	public ResponseEntity<Object> addPolicyDetails(MasterPolicyDataEntity masterPolicyDataEntity) throws Exception {
		Map<String, Object> response = new HashMap<String, Object>();
		Map<String, Object> response1 = new HashMap<String, Object>();
		response.put(Constant.STATUS, 0);
		response.put(Constant.MESSAGE, Constant.SOMETHING_WENT_WRONG);
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		logger.debug("Enter masterPolicyData : " + methodName);

		try {
			if (masterPolicyDataEntity == null) {
				response.put(Constant.STATUS, 201);
				response.put(Constant.MESSAGE, Constant.NO_DATA_FOUND);               
				response1.put("body", response); 
				return new ResponseEntity<Object>(response1, HttpStatus.CREATED);
			}
				 else {
					MasterPolicyDataEntity policyData = masterPolicyDataRepository.save(masterPolicyDataEntity);
					response.put(Constant.STATUS, 200);
					response.put(Constant.MESSAGE, Constant.SUCCESS);
					response.put("MasterPolicyRecordId", policyData.getMasterpolicyid());
					response1.put("body", response); 
					return new ResponseEntity<Object>(response1, HttpStatus.OK);
				}
			

		} catch (Exception exception) {
			logger.error("Could not add master policy due to " + exception.getMessage());
			exception.printStackTrace();
			 throw new MphBankServiceException ("Internal Server Error");
		}
		//return null;

	}

	@Override
	public MphNameEntity getMphnameBasedOnPolicyNumber(String policyNumber) throws Exception {
		logger.info("searchBankDetails method started");
		try
		{
			return mphNameRepository.findMphDetailsUsingPolicyNumber(policyNumber);
		}
		catch(Exception e)
		{
			logger.debug("Exception Occurred while fetching MPH Details ::"+e.getMessage());
		}
		logger.info("searchBankDetails method ended");
		return null;
	}

	
}