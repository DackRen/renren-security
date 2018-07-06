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

import io.renren.common.specification.RSQLSpecification;
import io.renren.modules.sys.entity.SysUserEntity;
import org.springframework.data.domain.Page;import io.renren.common.base.ServiceImpl;import io.renren.common.annotation.DataFilter;
import io.renren.common.utils.Constant;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;
import io.renren.modules.sys.dao.SysRoleDao;
import io.renren.modules.sys.entity.SysDeptEntity;
import io.renren.modules.sys.entity.SysRoleEntity;
import io.renren.modules.sys.service.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;


/**
 * 角色
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2016年9月18日 上午9:45:12
 */
@Service("sysRoleService")
public class SysRoleServiceImpl extends ServiceImpl<SysRoleEntity, Long> implements SysRoleService {
	private final SysRoleMenuService sysRoleMenuService;
	private final SysRoleDeptService sysRoleDeptService;
	private final SysUserRoleService sysUserRoleService;
	private final SysDeptService sysDeptService;
	private final SysRoleDao repository;

	@Autowired
	public SysRoleServiceImpl(SysRoleDao repository, SysRoleMenuService sysRoleMenuService, SysRoleDeptService sysRoleDeptService, SysUserRoleService sysUserRoleService, SysDeptService sysDeptService) {
		super(repository);
		this.sysRoleMenuService = sysRoleMenuService;
		this.sysRoleDeptService = sysRoleDeptService;
		this.sysUserRoleService = sysUserRoleService;
		this.sysDeptService = sysDeptService;
		this.repository = repository;
	}

	@Override
	@DataFilter(subDept = true, user = false)
	public PageUtils queryPage(Map<String, Object> params) {
		String roleName = (String)params.get("roleName");

		Page<SysUserEntity> page = repository.findAll(
				new RSQLSpecification<>("roleName", "like", roleName), new Query<SysUserEntity>(params).getPage());
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
	public SysRoleEntity save(SysRoleEntity role) {
		role.setCreateTime(new Date());
		role = super.save(role);

		//保存角色与菜单关系
		sysRoleMenuService.saveOrUpdate(role.getRoleId(), role.getMenuIdList());

		//保存角色与部门关系
		sysRoleDeptService.saveOrUpdate(role.getRoleId(), role.getDeptIdList());

		return role;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void update(SysRoleEntity role) {
		super.save(role);

		//更新角色与菜单关系
		sysRoleMenuService.saveOrUpdate(role.getRoleId(), role.getMenuIdList());

		//保存角色与部门关系
		sysRoleDeptService.saveOrUpdate(role.getRoleId(), role.getDeptIdList());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteBatch(Long[] roleIds) {
		//删除角色
		repository.deleteAllByRoleIdIn(roleIds);

		//删除角色与菜单关联
		sysRoleMenuService.deleteBatch(roleIds);

		//删除角色与部门关联
		sysRoleDeptService.deleteBatch(roleIds);

		//删除角色与用户关联
		sysUserRoleService.deleteBatch(roleIds);
	}


}
