package com.netcorner.velocity.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;


/**
 * Created by shijiufeng on 2022/4/22.
 */
@RestController
public class HelloController{

    @ResponseBody
    @RequestMapping(value="/hello",method = RequestMethod.GET)
    public ModelAndView hello() {
        ModelAndView modelAndView=new ModelAndView("/hello");
        modelAndView.addObject("username","netcorner");
        return modelAndView;
    }




}
