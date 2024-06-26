package com.fish.vwhub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName vw_hub_output
 */
@TableName(value ="vw_hub_output")
@Data
public class VwHubOutput implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    private Object inputId;

    /**
     * 
     */
    private String outputFileName;

    /**
     * 
     */
    private Date gmtCreate;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}