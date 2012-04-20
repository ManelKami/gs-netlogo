package org.graphstream.netlogo.extension.sender;

import org.graphstream.netlogo.extension.GSManager;
import org.nlogo.api.Agent;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultCommand;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.Link;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.Observer;
import org.nlogo.api.Syntax;
import org.nlogo.api.Turtle;

public class SendAddAttribute extends DefaultCommand {
	@Override
	public String getAgentClassString() {
		return "OTL";
	}

	@Override
	public Syntax getSyntax() {
		return Syntax.commandSyntax(new int[] {
				Syntax.TYPE_STRING,
				Syntax.TYPE_STRING,
				Syntax.TYPE_NUMBER | Syntax.TYPE_BOOLEAN | Syntax.TYPE_STRING
						| Syntax.TYPE_LIST });
	}

	@Override
	public void perform(Argument[] args, Context context)
			throws ExtensionException, LogoException {
		try {
			String senderId = args[0].getString();
			GSSender sender = GSManager.getSender(senderId);
			if (sender == null)
				return;
			String attribute = args[1].getString();
			Object value = argToNetStream(args[2]);
			Agent agent = context.getAgent();
			if (agent instanceof Observer)
				sender.sendGraphAttributeAdded(attribute, value);
			else if (agent instanceof Turtle)
				sender.sendNodeAttributeAdded(agent.id(), attribute, value);
			else if (agent instanceof Link) {
				Link link = (Link) agent;
				sender.sendEdgeAttributeAdded(link.end1().id(), link.end2()
						.id(), attribute, value);
			}
		} catch (LogoException e) {
			throw new ExtensionException(e.getMessage());
		}
	}

	protected static Object argToNetStream(Argument arg)
			throws ExtensionException, LogoException {
		Object value = arg.get();
		if (isOfSimpleType(value))
			return value;

		LogoList list = arg.getList();
		if (list.isEmpty())
			throw new ExtensionException("The list must not be empty");
		Class<?> elementClass = list.get(0).getClass();
		for (Object o : list) {
			if (!isOfSimpleType(o))
				throw new ExtensionException(
						"The list elements must be of type boolean, number or string");
			if (!o.getClass().equals(elementClass))
				throw new ExtensionException(
						"The list elements must be all of same type");
		}
		return list.toArray();
	}

	protected static boolean isOfSimpleType(Object o) {
		return o != null
				&& (o instanceof Boolean || o instanceof Double || o instanceof String);
	}
}
