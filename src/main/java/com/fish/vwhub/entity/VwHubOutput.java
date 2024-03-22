package com.fish.vwhub.entity;

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
    @TableId
    private Object id;

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

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        VwHubOutput other = (VwHubOutput) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getInputId() == null ? other.getInputId() == null : this.getInputId().equals(other.getInputId()))
            && (this.getOutputFileName() == null ? other.getOutputFileName() == null : this.getOutputFileName().equals(other.getOutputFileName()))
            && (this.getGmtCreate() == null ? other.getGmtCreate() == null : this.getGmtCreate().equals(other.getGmtCreate()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getInputId() == null) ? 0 : getInputId().hashCode());
        result = prime * result + ((getOutputFileName() == null) ? 0 : getOutputFileName().hashCode());
        result = prime * result + ((getGmtCreate() == null) ? 0 : getGmtCreate().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", inputId=").append(inputId);
        sb.append(", outputFileName=").append(outputFileName);
        sb.append(", gmtCreate=").append(gmtCreate);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}