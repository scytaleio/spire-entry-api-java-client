package io.spiffe.entryapi;

public interface EntryService {

    DefaultEntryService.CreateResult createEntry(DefaultEntryService.EntryDef entryDef);
}
