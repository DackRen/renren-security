/**
 * Copyright 2018 人人开源 http://www.renren.io
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.renren.modules.sys.service.impl;


import io.renren.common.exception.RRException;
import io.renren.common.specification.RSQLBuilder;
import io.renren.common.specification.RSQLSpecification;
import io.renren.common.validator.Assert;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;import io.renren.common.base.ServiceImpl;import io.renren.common.annotation.DataFilter;
import io.renren.common.base.ServiceImpl;
import io.renren.common.utils.Constant;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;
import io.renren.modules.sys.dao.SysUserDao;
import io.renren.modules.sys.entity.SysDeptEntity;
import io.renren.modules.sys.entity.SysUserEntity;
import io.renren.modules.sys.service.SysDeptService;
import io.renren.modules.sys.service.SysUserRoleService;
import io.renren.modules.sys.service.SysUserService;
import io.renren.modules.sys.shiro.ShiroUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 系统用户
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2016年9月18日 上午9:46:09
 */
@Service("sysUserService")
public class SysUserServiceImpl extends ServiceImpl<SysUserEntity, Long> implements SysUserService {
	private final SysUserRoleService sysUserRoleService;
	private final SysDeptService sysDeptService;
	private final SysUserDao repository;

	@Autowired
	public SysUserServiceImpl(SysUserRoleService sysUserRoleService, SysDeptService sysDeptService, SysUserDao repository) {
		super(repository);
		this.sysUserRoleService = sysUserRoleService;
		this.sysDeptService = sysDeptService;
		this.repository = repository;
	}


	/**
	 * 查询用户的所有菜单ID
	 */
	@Override
	public List<Long> queryAllMenuId(Long userId) {
//		return repository.queryAllMenuId(userId);
		return new ArrayList<>();
	}

	@Override
	@DataFilter(subDept = true, user = false)
	public PageUtils queryPage(Map<String, Object> params) {
		String username = (String)params.get("username");

		Page<SysUserEntity> page = repository.findAll(
				new RSQLSpecification<>("username", "like", username), new Query<SysUserEntity>(params).getPage());
//			new EntityWrapper<SysUserEntity>()
//				.like(StringUtils.isNotBlank(username),"username", username)
//				.addFilterIfNeed(params.get(Constant.SQL_FILTER) != null, (String)params.get(Constant.SQL_FILTER))

		for(SysUserEntity sysUserEntity : page.getContent()){
			SysDeptEntity sysDeptEntity = sysDeptService.findById(sysUserEntity.getDeptId());
			sysUserEntity.setDeptName(sysDeptEntity.getName());
		}

		return new PageUtils(page);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public SysUserEntity save(SysUserEntity user) {
		user.setCreateTime(new Date());
		//sha256加密
		String salt = RandomStringUtils.randomAlphanumeric(20);
		user.setSalt(salt);
		user.setPassword(ShiroUtils.sha256(user.getPassword(), user.getSalt()));
		user = super.save(user);
		
		//保存用户与角色关系
		sysUserRoleService.saveOrUpdate(user.getUserId(), user.getRoleIdList());
		return user;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void update(SysUserEntity user) {
		if (!repository.existsById(user.getUserId())) {
			throw new RRException("更新失败, 用户未找到");
		}
		if(StringUtils.isBlank(user.getPassword())){
			user.setPassword(null);
		}else{
			user.setPassword(ShiroUtils.sha256(user.getPassword(), user.getSalt()));
		}
		super.save(user);
		
		//保存用户与角色关系
		sysUserRoleService.saveOrUpdate(user.getUserId(), user.getRoleIdList());
	}


	@Override
	public boolean updatePassword(Long userId, String password, String newPassword) {
        SysUserEntity userEntity = findById(userId);
		if (!StringUtils.equals(userEntity.getPassword(), password)) {
			throw new RRException("密码错误");
		}
        userEntity.setPassword(newPassword);
		this.update(userEntity);
        return true;
    }

}
