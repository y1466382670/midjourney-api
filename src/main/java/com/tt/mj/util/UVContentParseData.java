package com.tt.mj.util;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UVContentParseData extends ContentParseData {
	protected Integer index;
}
