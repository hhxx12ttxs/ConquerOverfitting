package honeycrm.server.test.medium;

import honeycrm.client.dto.Dto;
import honeycrm.client.dto.ListQueryResult;
import honeycrm.client.misc.Timer;
import honeycrm.server.domain.Account;
import honeycrm.server.domain.Contact;
import honeycrm.server.domain.Contract;
import honeycrm.server.domain.Product;
import honeycrm.server.domain.UniqueService;
import honeycrm.server.services.ReadServiceImpl;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.gwt.user.client.Command;

public class ReadServiceTest extends DatastoreTest {
	private ReadServiceImpl reader;
	private static final DatastoreService db = DatastoreServiceFactory.getDatastoreService();
	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

	@Override
	protected void setUp() throws Exception {
		helper.setUp();
		reader = new ReadServiceImpl(); // re-create read service between tests to avoid full text search cache invalidation
	}

	@Override
	protected void tearDown() throws Exception {
		helper.tearDown();
		reader = null;
	}

	public void testGet() {
		final Entity e = createContact();
		final Dto dto = reader.get(Contact.class.getSimpleName(), e.getKey().getId());

		assertEquals(e.getProperty("name"), dto.get("name"));
	}

	public void testGetAll() {
		for (int i = 0; i < 100; i++) {
			createContact();
		}

		final ListQueryResult r = reader.getAll(Contact.class.getSimpleName(), 0, 100);
		assertEquals(100, r.getItemCount());
		assertEquals(100, r.getResults().length);
	}

	public void testFulltextSearchOnSingleModule() {
		final Dto contact = new Dto(Contact.class.getSimpleName());
		for (int i = 0; i < 10; i++) {
			contact.set("name", "foo" + i);
			createService.create(contact);
		}

		final ListQueryResult r = reader.fulltextSearchForModule(Contact.class.getSimpleName(), "foo", 0, 100);

		assertEquals(10, r.getItemCount());

		final ListQueryResult r2 = reader.fulltextSearchForModule(Contact.class.getSimpleName(), "foo1", 0, 100);

		assertEquals(1, r2.getItemCount());
	}

	public void testFulltextSearchOnAllModules() {
		for (int i = 0; i < 10; i++) {
			final Dto c = new Dto("Contact");
			final Dto a = new Dto("Account");
			final Dto p = new Dto("Product");

			c.set("name", "contact" + i);
			a.set("name", "account" + i);
			p.set("name", "product" + i);

			if (i % 2 == 0) { // every second product has a description too
				p.set("description", "product description 42 " + i);
			}

			createService.create(c);
			createService.create(a);
			createService.create(p);
		}

		assertEquals(3, reader.fulltextSearch("1", 0, 100).getItemCount());
		assertEquals(10, reader.fulltextSearch("contact", 0, 100).getItemCount());
		assertEquals(10, reader.fulltextSearch("product", 0, 100).getItemCount());
		assertEquals(5, reader.fulltextSearch("42", 0, 100).getItemCount());

	}

	public void testGetByName() {
		final Entity e = createContact();

		final Dto dto = reader.getByName(Contact.class.getSimpleName(), e.getProperty("name").toString());
		assertEquals(e.getProperty("name").toString(), dto.get("name").toString());

		assertNull(reader.getByName(Contact.class.getSimpleName(), String.valueOf(random.nextDouble())));
		assertNull(reader.getByName(Contact.class.getSimpleName(), null));
	}

	public void testGetWithReferences() {
		final Entity account = createAccount();
		final Entity contact = createContact();
		contact.setProperty("accountId", account.getKey());
		db.put(contact);

		final Dto cwa = reader.get(Contact.class.getSimpleName(), contact.getKey().getId());
		assertTrue(null != cwa.get("accountId_resolved") && ((Dto) cwa.get("accountId_resolved")).get("name").equals(account.getProperty("name")));
	}

	public void testGetWithLists() {
		final Entity product = new Entity(Product.class.getSimpleName());
		product.setProperty("name", "product" + random.nextLong());
		db.put(product);

		final Entity uniqueService = new Entity(UniqueService.class.getSimpleName());
		uniqueService.setProperty("productID", product.getKey());
		db.put(uniqueService);

		final ArrayList<Key> serviceKeys = new ArrayList<Key>(1);
		serviceKeys.add(uniqueService.getKey());

		final Entity contract = new Entity(Contract.class.getSimpleName());
		contract.setProperty("uniqueServices", serviceKeys);
		db.put(contract);

		final Dto contractDto = reader.get(Contract.class.getSimpleName(), contract.getKey().getId());
		assertEquals(1, ((List<?>) contractDto.get("uniqueServices")).size());
		assertTrue(((List<?>) contractDto.get("uniqueServices")).get(0) instanceof Dto);
		assertEquals(product.getKey().getId(), ((List<Dto>) contractDto.get("uniqueServices")).get(0).get("productID"));
	}

	public void testGetPerformance() throws InterruptedException {
		final float count = 1000;

		System.out.print("Creating key array with " + count + " elements.. ");
		final long[] keys = new long[(int) count];
		System.out.println("Done.");

		final long createTime = Timer.getTime(new Command() {
			@Override
			public void execute() {
				for (int i = 0; i < count; i++) {
					keys[i] = createContact().getKey().getId();
				}
			}
		});
		System.out.println("Created " + count + " entities in " + createTime + "ms (" + (count / createTime) + " entities/ms)");

		final float iterations = 10;
		for (int i = 0; i < iterations; i++) {
			final long randomGetTime = Timer.getTime(new Command() {
				@Override
				public void execute() {
					for (int i = 0; i < count; i++) {
						final long randomId = keys[random.nextInt((int) count)];
						reader.get("Contact", randomId);
					}
				}
			});
			System.out.println("Got " + count + " random entities " + (1 + i) + "/" + iterations + " in " + randomGetTime + "ms (" + (count / randomGetTime) + " entities/ms)");
		}

		for (int i = 0; i < iterations; i++) {
			final long getAllTime = Timer.getTime(new Command() {
				@Override
				public void execute() {
					reader.getAll("Contact", 0, (int) count);
				}
			});
			System.out.println("getAll " + (1 + i) + "/" + iterations + " times for " + count + " entities in " + getAllTime + "ms (" + (count / getAllTime) + " entities/ms)");
		}
		// Thread.sleep(1000 * 60 * 60);
	}
	
	public void testGetEntityContainingCycle() {
		final Dto p1 = new Dto(Product.class.getSimpleName());
		p1.set("name", "p1");
		final long p1Id = createService.create(p1);
		
		final Dto p2 = new Dto(Product.class.getSimpleName());
		p2.set("name", "p2");
		p2.set("predecessor", p1Id);
		final long p2Id = createService.create(p2);
		
		Dto p1Retrieved = reader.get(Product.class.getSimpleName(), p1Id);
		p1Retrieved.set("predecessor", p2Id);
		updateService.update(p1Retrieved);
		
		reader.get(Product.class.getSimpleName(), p1Id);
	}
	
	public void testGetEntityWithNonExistingReference() {
		final Dto p1 = new Dto(Product.class.getSimpleName());
		p1.set("name", "p1");
		final long p1Id = createService.create(p1);
		
		final Dto p2 = new Dto(Product.class.getSimpleName());
		p2.set("name", "p2");
		p2.set("predecessor", p1Id);
		final long p2Id = createService.create(p2);
		
		deleteService.delete(Product.class.getSimpleName(), p1Id);
		
		final Dto retrievedP2 = reader.get(Product.class.getSimpleName(), p2Id);
		assertNotNull(retrievedP2);
	}

	private Entity createContact() {
		final String suffix = String.valueOf(random.nextLong());
		final Entity e = new Entity(Contact.class.getSimpleName());
		e.setProperty("email", "email" + suffix);
		e.setProperty("name", "name" + suffix);
		e.setProperty("city", "city" + suffix);
		e.setProperty("phone", "phone" + suffix);
		e.setProperty("emailOptedOut", random.nextBoolean());
		e.setProperty("doNotCall", random.nextBoolean());
		e.setProperty("mobile", "mobile" + suffix);
		e.setProperty("doNotCallExplanation", "explanation" + suffix);
		e.setProperty("bankAccountData", "bank" + suffix);
		e.setProperty("profession", "Student" + suffix);
		e.setProperty("study", "Biology" + suffix);
		db.put(e);
		return e;
	}

	private Entity createAccount() {
		final Entity e = new Entity(Account.class.getSimpleName());
		e.setProperty("name", "some account" + random.nextLong());
		db.put(e);
		return e;
	}

}

