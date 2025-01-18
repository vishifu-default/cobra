import React from 'react';
import { ContentHeading, TrackConsumerModal } from '../../components';
import './configuration.scss';
import {
  Button,
  DataTable,
  Table,
  TableContainer,
  TableHead,
  TableHeader,
  TableBody,
  TableCell,
  TableRow,
  TableToolbar,
  TableToolbarContent,
  TableSelectRow,
  TableSelectAll
} from '@carbon/react';
import { useConsumerStore } from '../../store/use-consumer-store';

function Configuration() {
  const headers = [
    { key: 'domain', header: 'Domain' },
    { key: 'port', header: 'Port' }
  ];

  const { consumers } = useConsumerStore();

  const [configureConsumerModel, setConfigureConsumerModel] = React.useState(false);

  function openConsumerConfigurationModal() {
    setConfigureConsumerModel(true);
  }

  function closeConsumerConfigurationModal() {
    setConfigureConsumerModel(false);
  }

  function action(e) {
    console.log(e);
  }

  return (
    <>
      <ContentHeading displayName={'Configuration'} />

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
                <Button onClick={openConsumerConfigurationModal}>Add configuration</Button>
              </TableToolbarContent>
            </TableToolbar>
            <Table {...getTableProps()} aria-label='sample table'>
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
                    })}
                    onClick={(evt) => action(evt)}>
                    <TableSelectRow
                      {...getSelectionProps({
                        row
                      })}
                      onChange={(e) => action(e)}
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
}

export default Configuration;
