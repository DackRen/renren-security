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
import org.springframework.data.domain.Page;import io.renren.common.base.ServiceImpl;import com.google.gson.Gson;
import io.renren.common.exception.RRException;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;
import io.renren.modules.sys.dao.SysConfigDao;
import io.renren.modules.sys.entity.SysConfigEntity;
import io.renren.modules.sys.redis.SysConfigRedis;
import io.renren.modules.sys.service.SysConfigService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Map;

@Service("sysConfigService")
public class SysConfigServiceImpl extends ServiceImpl<SysConfigEntity, Long> implements SysConfigService {
	private final SysConfigRedis sysConfigRedis;
	private final SysConfigDao repository;

	@Autowired
	public SysConfigServiceImpl(SysConfigDao repository, SysConfigRedis sysConfigRedis) {
		super(repository);
		this.sysConfigRedis = sysConfigRedis;
		this.repository = repository;
	}

	@Override
	public PageUtils queryPage(Map<String, Object> params) {
		String paramKey = (String)params.get("paramKey");

		Page<SysConfigEntity> page = repository.findAll(
				new RSQLSpecification<SysConfigEntity>("status", "eq", 1).and("paramKey", "like", paramKey),
				new Query<SysConfigEntity>(params).getPage());
//				new EntityWrapper<SysConfigEntity>()
//					.like(StringUtils.isNotBlank(paramKey),"param_key", paramKey)
//					.eq("status", 1)

		return new PageUtils(page);
	}
	
	@Override
	public SysConfigEntity save(SysConfigEntity config) {
		config = super.save(config);
		sysConfigRedis.saveOrUpdate(config);
		return config;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void update(SysConfigEntity config) {
		super.save(config);
		sysConfigRedis.saveOrUpdate(config);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateValueByKey(String key, String value) {
		SysConfigEntity sysConfigEntity = repository.findByParamKey(key);
		sysConfigEntity.setParamValue(value);
		repository.save(sysConfigEntity);
		sysConfigRedis.delete(key);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteBatch(Long[] ids) {
		for(Long id : ids){
			SysConfigEntity config = this.findById(id);
			sysConfigRedis.delete(config.getParamKey());
		}
		repository.deleteInBatch(repository.findAllById(Arrays.asList(ids)));
	}

	@Override
	public String getValue(String key) {
		SysConfigEntity config = sysConfigRedis.get(key);
		if(config == null){
			config = repository.findByParamKey(key);
			sysConfigRedis.saveOrUpdate(config);
		}

		return config == null ? null : config.getParamValue();
	}
	
	@Override
	public <T> T getConfigObject(String key, Class<T> clazz) {
		String value = getValue(key);
		if(StringUtils.isNotBlank(value)){
			return new Gson().fromJson(value, clazz);
		}

		try {
			return clazz.newInstance();
		} catch (Exception e) {
			throw new RRException("获取参数失败");
		}
	}
}
