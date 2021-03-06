package com.noob.state.util;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.noob.state.constants.Symbol;
import com.noob.state.entity.Meta;
import com.noob.state.entity.adapter.Adapter;
import com.noob.state.monitor.Monitor;
import com.noob.state.monitor.MonitorFactory.EventSource;
import com.noob.state.monitor.MonitorFactory.MonitorContainer;

public class StateUtil {

	/**
	 * 节点信息转换
	 */
	public static String exchange(String transferData, String localData, EventSource source) {
		String updateData;
		if (Strings.isNullOrEmpty(localData)) // 本身无状态
			updateData = transferData;
		else {
			List<String> removeInfo = greaterLevel(source);
			updateData = exchange(localData, transferData, removeInfo);

		}

		return updateData == null ? Symbol.EMPTY : updateData;
	}

	/**
	 * 整理出哪些要更新 哪些要删除
	 *
	 * @param localData
	 *            本地信息
	 * @param transferData
	 *            需要更新的信息
	 * @param removeInfo
	 *            需要删除的信息
	 */
	private static String exchange(String localData, String transferData, List<String> removeInfo) {
		List<String> localList = split(localData);
		List<String> transferList = split(transferData);

		if (CommonUtil.notEmpty(transferList)) {
			Iterator<String> updateIterator = transferList.iterator();
			while (updateIterator.hasNext()) {
				String each = updateIterator.next();
				if (localList.contains(each)) {
					updateIterator.remove();
					removeInfo.remove(each);
				}
			}

		}
		if (CommonUtil.notEmpty(removeInfo)) localList.removeAll(removeInfo);
		if (CommonUtil.notEmpty(transferList)) localList.addAll(transferList);
		return joinToStr(localList);

	}

	/**
	 * 分割监控状态信息
	 */
	public static List<String> split(String info) {
		return Strings.isNullOrEmpty(info) ? null
				: Splitter.on(Symbol.SEMICOLON).omitEmptyStrings().splitToList(info).stream()
						.distinct().collect(Collectors.toList());
	}

	/**
	 * 获取比指定优先级更高的监控源对应的会更新在节点数据上的监控状态
	 */
	public static List<String> greaterLevel(EventSource localSource) {
		List<String> result = null;
		for (EventSource source : EventSource.values()) {
			if (source.getLevel() < localSource.getLevel()) {
				List<String> singleList = MonitorContainer.getDisplayMonitor(source);
				if (result == null) result = Lists.newArrayList();
				if (singleList != null) result.addAll(singleList);
			}
		}

		return result;
	}

	/**
	 * 拼接监控状态
	 */
	public static String join(List<Monitor> list) {
		return CommonUtil.notEmpty(list)
				? joinToStr(list.stream().map(Monitor::toString).collect(Collectors.toList()))
				: Symbol.EMPTY;
	}

	public static String joinToStr(List<String> list) {
		return String.join(Symbol.SEMICOLON, list);
	}

	/**
	 * 转换监控状态为对象
	 */
	public static List<Monitor> splitToMonitorList(String info) {
		List<String> infoList = split(info);

		return CommonUtil.notEmpty(infoList) ? infoList.stream().map(StateUtil::convert).distinct()
				.filter(t -> t != null).collect(Collectors.toList()) : null;
	}

	private static Monitor convert(String info) {
		return Strings.isNullOrEmpty(info) ? null
				: CommonUtil.getObjFromOptional(MonitorContainer.getAllMonitor().stream()
						.filter(t -> t.toString().equals(info)).findFirst());
	}

	/**
	 * 增加单个监控
	 */
	public static String addSingleMonitor(String localInfo, Monitor monitor) {
		return Strings.isNullOrEmpty(localInfo) ? monitor.toString()
				: localInfo.contains(monitor.toString()) ? null
						: String.join(Symbol.SEMICOLON, localInfo, monitor.toString());
	}

	/**
	 * 删除单个监控
	 */
	public static String removeSingleMonitor(String localInfo, Monitor monitor) {
		if (Strings.isNullOrEmpty(localInfo)) return null;
		List<String> localList = Splitter.on(Symbol.SEMICOLON).omitEmptyStrings()
				.splitToList(localInfo).stream().distinct().collect(Collectors.toList());

		String info = monitor.toString();
		boolean remove = false;
		if (localInfo.contains(info)) {
			localList.remove(info);
			remove = true;
		}

		return remove ? String.join(Symbol.SEMICOLON, localList) : null;

	}

	/**
	 * 更新本地状态
	 *
	 * @param data
	 *            新的状态信息
	 * @param adapter
	 *            老的状态信息
	 */
	public static <T extends Meta> void updateLocalCache(String data, Adapter<T> adapter) {
		if (adapter != null) {
			adapter.getMonitorList().clear();
			List<Monitor> monitor = splitToMonitorList(data);
			if (monitor != null) adapter.getMonitorList().addAll(monitor);
		}

	}

}
