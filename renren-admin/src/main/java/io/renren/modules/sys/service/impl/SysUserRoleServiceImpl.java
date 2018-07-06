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


import io.renren.common.base.ServiceImpl;
import io.renren.common.specification.RSQLSpecification;
import io.renren.common.utils.MapUtils;
import io.renren.modules.sys.dao.SysUserRoleDao;
import io.renren.modules.sys.entity.SysUserRoleEntity;
import io.renren.modules.sys.service.SysUserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * 用户与角色对应关系
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2016年9月18日 上午9:45:48
 */
@Service("sysUserRoleService")
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleEntity, Long> implements SysUserRoleService {
	private final SysUserRoleDao repository;

	@Autowired
	public SysUserRoleServiceImpl(SysUserRoleDao repository) {
		super(repository);
		this.repository = repository;
	}

	@Override
	public void saveOrUpdate(Long userId, List<Long> roleIdList) {
		//先删除用户与角色关系
		repository
				.findOne(Example.of(SysUserRoleEntity.builder().userId(userId).build()))
				.ifPresent(sysUserRoleEntity -> repository.delete(sysUserRoleEntity));

		if(roleIdList == null || roleIdList.size() == 0){
			return ;
		}
		
		//保存用户与角色关系
		List<SysUserRoleEntity> list = new ArrayList<>(roleIdList.size());
		for(Long roleId : roleIdList){
			SysUserRoleEntity sysUserRoleEntity = new SysUserRoleEntity();
			sysUserRoleEntity.setUserId(userId);
			sysUserRoleEntity.setRoleId(roleId);

			list.add(sysUserRoleEntity);
		}
		repository.saveAll(list);
	}

	@Override
	public List<Long> queryRoleIdList(Long userId) {
		return repository.findAllByUserId(userId);
	}

	@Override
	public int deleteBatch(Long[] roleIds){
//		List<SysUserRoleEntity> sysUserRoleEntities = repository.findAll(new RSQLSpecification<>("roleId", "in", roleIds));
		return repository.deleteAllByRoleIdIn(roleIds);
//		return sysUserRoleEntities.size();
	}
}
