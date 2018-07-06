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

package io.renren.modules.sys.entity;

import io.renren.common.base.AbstractEntity;
import lombok.Data;
import org.hibernate.annotations.Loader;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;


/**
 * 部门管理
 * 
 * @author chenshun
 * email: sunlightcs@gmail.com
 * @date 2017-06-20 15:23:47
 */
//@SQLDelete(sql =
//		"UPDATE sys_dept SET delFlag = -1 WHERE id = ?")
//@Loader(namedQuery = "findSysDeptById")
//@NamedQuery(name = "findSysDeptById", query = "SELECT t " +
//		"FROM SysDeptEntity t " +
//		"WHERE " +
//		"   t.id = ?1 AND " +
//		"   t.delFlag = 0")
//@Where(clause = "delFlag = 0")
@Table(name = "sys_dept")
@Data
@Entity
public class SysDeptEntity extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	
	//部门ID
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long deptId;
	//上级部门ID，一级部门为0
	private Long parentId;
	//部门名称
	private String name;
	//上级部门名称
	@Transient
	private String parentName;
	//排序
	private Integer orderNum;

//	@TableLogic
//	@Transient
	private Integer delFlag;
	/**
	 * ztree属性
	 */
	@Transient
	private Boolean open;
	@Transient
	private List<?> list;
}
