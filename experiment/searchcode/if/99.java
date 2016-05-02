package logger.sd.server.service;

import logger.sd.protocol.LRP;
import logger.sd.server.LoggingServer;

public class TokenArriveService implements Service {

	private String request;
	private LoggingServer server;

	public TokenArriveService(String request, LoggingServer server) {
		this.request = request;
		this.server = server;
	}

	@Override
	public String trataRequisicao() {
		if (request.equals(LRP.TOKENARRIVE.getMessage())) {
			server.tokenArrive();
			return LRP.okMessage();
		} else {
			return LRP.error01();
		}
	}

	@Override
	public String getRequest() {
		return request;
	}

	@Override
	public String getCode() {
		return LRP.TOKEN.getMessage();
	}

}

