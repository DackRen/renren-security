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

import org.springframework.data.jpa.repository.JpaRepository;
import io.renren.modules.sys.entity.SysRoleDeptEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色与部门对应关系
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2017年6月21日 23:33:46
 */
@Repository
public interface SysRoleDeptDao extends JpaRepository<SysRoleDeptEntity, Long> {
	
	/**
	 * 根据角色ID，获取部门ID列表
	 */
	List<SysRoleDeptEntity> findAllByRoleIdIn(Long[] roleIds);

	/**
	 * 根据角色ID数组，批量删除
	 */
	int deleteAllByRoleIdIn(Long[] roleIds);
}
