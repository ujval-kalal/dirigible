package org.eclipse.dirigible.cms.internal;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.dirigible.cms.api.ICmsProvider;
import org.eclipse.dirigible.commons.config.Configuration;
import org.eclipse.dirigible.repository.api.IRepository;
import org.eclipse.dirigible.repository.local.LocalRepository;

public class CmsProviderInternal implements ICmsProvider {

	/** The Constant DIRIGIBLE_CMS_INTERNAL_ROOT_FOLDER. */
	public static final String DIRIGIBLE_CMS_INTERNAL_ROOT_FOLDER = "DIRIGIBLE_CMS_INTERNAL_ROOT_FOLDER"; //$NON-NLS-1$

	/** The Constant DIRIGIBLE_CMS_INTERNAL_ROOT_FOLDER_IS_ABSOLUTE. */
	public static final String DIRIGIBLE_CMS_INTERNAL_ROOT_FOLDER_IS_ABSOLUTE = "DIRIGIBLE_CMS_INTERNAL_ROOT_FOLDER_IS_ABSOLUTE"; //$NON-NLS-1$

	private static final String CMIS = "cmis";

	/** The Constant NAME. */
	public static final String NAME = "repository";

	/** The Constant TYPE. */
	public static final String TYPE = "internal";

	private static final Map<String, CmisSession> SESSIONS = Collections
			.synchronizedMap(new HashMap<String, CmisSession>());

	private CmisRepository cmisRepository;

	public CmsProviderInternal() {
		Configuration.load("/dirigible-cms-internal.properties");

		String rootFolder = Configuration.get(DIRIGIBLE_CMS_INTERNAL_ROOT_FOLDER);
		boolean absolute = Boolean.parseBoolean(Configuration.get(DIRIGIBLE_CMS_INTERNAL_ROOT_FOLDER_IS_ABSOLUTE));

		String repositoryFolder = rootFolder + File.separator + CMIS;

		IRepository repository = new LocalRepository(repositoryFolder, absolute);
		CmisRepository cmisRepository = CmisRepositoryFactory.createCmisRepository(repository);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public Object getSession() {
		CmisSession cmisSession = cmisRepository.getSession();
		return cmisSession;
	}

}