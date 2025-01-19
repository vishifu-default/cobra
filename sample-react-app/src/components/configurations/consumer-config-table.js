import {
  Button,
  DataTable,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableHeader,
  TableRow,
  TableSelectAll,
  TableSelectRow,
  TableToolbar,
  TableToolbarAction,
  TableToolbarContent,
  TableToolbarMenu
} from '@carbon/react';
import { TrackConsumerModal } from '../index';
import React from 'react';
import { useConsumerStore } from '../../store/use-consumer-store';

const ConsumerConfigTable = () => {
  const headers = [
    { key: 'domain', header: 'Domain' },
    { key: 'port', header: 'Port' }
  ];

  const { consumers, remove } = useConsumerStore();

  const [configureConsumerModel, setConfigureConsumerModel] = React.useState(false);
  const [selectedDomain, setSelectedDomain] = React.useState([]);

  const openConsumerConfigurationModal = () => {
    setConfigureConsumerModel(true);
  };

  const closeConsumerConfigurationModal = () => {
    setConfigureConsumerModel(false);
  };

  const handleSelectRow = (row, e) => {
    if (e) {
      setSelectedDomain([...selectedDomain, row.id]);
    } else {
      setSelectedDomain([...selectedDomain.filter((x) => x !== row.id)]);
    }
  };

  const handleRemoveSelectedDomain = (e) => {
    for (let selected of selectedDomain) {
      remove(selected);
    }
    setSelectedDomain([]);
  };
  return (
    <>
      <DataTable rows={consumers} headers={headers}>
        {({
          rows,
          headers,
          getHeaderProps,
          getRowProps,
          getSelectionProps,
          getTableProps,
          getTableContainerProps
        }) => (
          <TableContainer
            title='Consumers configuration'
            description='Consumer SpringBoot application configuration for tracking'
            {...getTableContainerProps()}>
            <TableToolbar aria-label={'Configuration toolbar'}>
              <TableToolbarContent>
                <TableToolbarMenu>
                  <TableToolbarAction onClick={(e) => handleRemoveSelectedDomain(e)}>
                    Remove selected
                  </TableToolbarAction>
                </TableToolbarMenu>
                <Button onClick={openConsumerConfigurationModal}>Add configuration</Button>
              </TableToolbarContent>
            </TableToolbar>
            <Table {...getTableProps()}>
              <TableHead>
                <TableRow>
                  <TableSelectAll {...getSelectionProps()} />
                  {headers.map((header, i) => (
                    <TableHeader
                      key={i}
                      {...getHeaderProps({
                        header
                      })}>
                      {header.header}
                    </TableHeader>
                  ))}
                </TableRow>
              </TableHead>
              <TableBody>
                {rows.map((row, i) => (
                  <TableRow
                    key={i}
                    {...getRowProps({
                      row
                    })}>
                    <TableSelectRow
                      {...getSelectionProps({
                        row
                      })}
                      onChange={(e) => handleSelectRow(row, e)}
                    />
                    {row.cells.map((cell) => (
                      <TableCell key={cell.id}>{cell.value}</TableCell>
                    ))}
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </DataTable>

      <TrackConsumerModal
        heading={'Add consumer configuration'}
        label={'Consumer'}
        open={configureConsumerModel}
        onClose={closeConsumerConfigurationModal}
      />
    </>
  );
};

export default ConsumerConfigTable;
