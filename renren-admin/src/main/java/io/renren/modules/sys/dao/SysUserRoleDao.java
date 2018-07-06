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

package io.renren.modules.sys.dao;

import io.renren.modules.sys.entity.SysUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import io.renren.modules.sys.entity.SysUserRoleEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户与角色对应关系
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2016年9月18日 上午9:34:46
 */
@Repository
public interface SysUserRoleDao extends JpaRepository<SysUserRoleEntity, Long>, JpaSpecificationExecutor<SysUserRoleEntity> {
	
	/**
	 * 根据用户ID，获取角色ID列表
	 */
	List<Long> findAllByUserId(Long userId);

	/**
	 * 根据角色ID数组，批量删除
	 */
	int deleteAllByRoleIdIn(Long[] roleIds);
}
