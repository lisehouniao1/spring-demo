package com.sm.project.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.sm.common.util.JedisUtils;

@Controller
public class IndexController {
	
	@Resource
	private JedisUtils jedisUtils;
	
	
	@RequestMapping("/index")
	public String toIndex(String name, Model model) {
		model.addAttribute("name", name);
		return "/index";
	}
	
	@RequestMapping(value = "/test/prop")
	@ResponseBody
	public String testProp() {
		jedisUtils.set("woshishui", "我是沈苗", 120);
		return JSON.toJSONString("成功！");
	}
	
}
